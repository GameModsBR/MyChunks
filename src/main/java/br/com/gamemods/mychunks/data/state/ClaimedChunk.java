package br.com.gamemods.mychunks.data.state;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
@NonnullByDefault
public class ClaimedChunk
{
    private final UUID worldId;
    private final Vector3i position;
    @Nullable
    private PlayerName owner;
    private Map<UUID, Member> members = new HashMap<>(0);
    private EnumSet<Permission> publicPermissions;

    public ClaimedChunk(UUID worldId, Vector3i position)
    {
        this.worldId = worldId;
        this.position = position;
        publicPermissions = EnumSet.copyOf(Permission.getDefaultPermissions());
    }

    public boolean check(Permission permission, Player player)
    {
        return check(permission, player, true);
    }

    public boolean check(Permission permission, Player player, boolean notify)
    {
        UUID playerUniqueId = player.getUniqueId();
        if(owner != null && owner.getUniqueId().equals(playerUniqueId))
            return true;

        Member member = members.get(playerUniqueId);
        if(member != null && member.getRank().getPermissions().contains(permission))
            return true;

        if(publicPermissions.contains(permission))
            return true;

        if(notify)
            permission.notifyFailure(player, owner);

        return false;
    }

    public UUID getWorldId()
    {
        return worldId;
    }

    public Vector3i getPosition()
    {
        return position;
    }

    @Nullable
    public PlayerName getOwner()
    {
        return owner;
    }

    public void setOwner(@Nullable PlayerName owner)
    {
        this.owner = owner;
    }
}
