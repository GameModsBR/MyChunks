package br.com.gamemods.mychunks.data.state;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
@NonnullByDefault
public class PermissionContext extends PublicContext implements Modifiable
{
    private Optional<PlayerName> owner = Optional.empty();
    private Map<UUID, Set<Member>> members = new HashMap<>(0);

    public PermissionContext()
    {}

    public PermissionContext(EnumMap<Permission, Boolean> publicPermissions)
    {
        super(publicPermissions);
    }

    @Override
    public void notifyFailure(Permission permission, Player player)
    {
        permission.notifyFailure(player, owner.orElse(PlayerName.ADMINS));
    }

    public boolean check(Permission permission, UUID playerUniqueId, boolean isAdmin)
    {
        PlayerName owner = this.owner.orElse(PlayerName.ADMINS);
        if(owner.getUniqueId().equals(playerUniqueId) || isAdmin && owner.equalsPlayer(PlayerName.ADMINS))
            return true;

        Set<Member> memberSet= members.get(playerUniqueId);
        if(memberSet != null)
            for(Member member: memberSet)
                if(member != null && member.getRank().getPermission(permission).orElse(false))
                    return true;

        return super.check(permission, playerUniqueId, isAdmin);
    }

    /**
     * The owner of this context, note that it can also be a fake player like {@link PlayerName#ADMINS}.
     */
    public Optional<PlayerName> getOwner()
    {
        return owner;
    }

    /**
     * Changes the owner of this context, the change is not persisted immediately
     * @param owner The new owner or {@code null} to refer to the server admins
     */
    public void setOwner(@Nullable PlayerName owner) throws UnsupportedOperationException
    {
        Optional<PlayerName> newValue = Optional.ofNullable(owner);

        modified |= !this.owner.equals(newValue);
        this.owner = newValue;
    }

    public void addMember(Member member)
    {
        UUID playerId = member.getPlayerId().getUniqueId();
        Set<Member> memberSet = members.get(playerId);
        if(memberSet == null) members.put(playerId, memberSet = new HashSet<>(1));
        modified |= memberSet.add(member);
    }

    public boolean removeMember(Member member)
    {
        UUID playerId = member.getPlayerId().getUniqueId();
        Set<Member> memberSet = members.get(playerId);
        if(memberSet == null)
            return false;
        boolean modified = memberSet.remove(member);
        if(memberSet.isEmpty())
            modified |= members.remove(playerId) != null;

        this.modified |= modified;
        return modified;
    }
}
