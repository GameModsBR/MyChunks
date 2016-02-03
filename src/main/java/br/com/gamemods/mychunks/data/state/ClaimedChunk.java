package br.com.gamemods.mychunks.data.state;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

/**
 * A chunk that is protected on the server, it can be claimed to a player or to the server admins.
 */
@ParametersAreNonnullByDefault
@NonnullByDefault
public class ClaimedChunk
{
    private final UUID worldId;
    private final Vector3i position;
    private PlayerName owner = PlayerName.ADMINS;
    private Map<UUID, Set<Member>> members = new HashMap<>(0);
    private EnumMap<Permission, Boolean> publicPermissions;
    @Nullable
    private Zone zone;

    /**
     * Construct a chunk that is claimed by the server admins, with no members and no public permission specified
     * @param worldId The world ID that this chunk resides
     * @param position The chunk position
     */
    public ClaimedChunk(UUID worldId, Vector3i position)
    {
        this.worldId = worldId;
        this.position = position;
        publicPermissions = new EnumMap<>(Permission.class);
    }

    /**
     * <p>Checks if this chunks declares an specific permission as public.</p>
     * Public permissions allows anyone to do this action regardless if the subject is a member or not.
     * @param permission The permission to be checked
     * @return {@code true} if it's permitted, {@code false} if it's denied or empty if it's not defined.
     */
    public Optional<Boolean> getPublicPermission(Permission permission)
    {
        return Optional.ofNullable(publicPermissions.get(permission));
    }

    /**
     * <p>Checks if a player has an specific permission on this chunk.</p>
     * <p>It checks the player public permissions, the player rank and the zone that this chunk resides.</p>
     * <p>The player will be notified if the permission is denied</p>
     * @param permission The permission to be checked
     * @param player The player that needs this permission
     * @return If the player has permission
     */
    public boolean check(Permission permission, Player player)
    {
        return check(permission, player, true);
    }

    /**
     * <p>Checks if a player has an specific permission on this chunk.</p>
     * <p>It checks the player public permissions, the player rank and the zone that this chunk resides.</p>
     * @param permission The permission to be checked
     * @param player The player that needs this permission
     * @param notify If the player should be notified if the permission is denied
     * @return If the player has permission
     */
    public boolean check(Permission permission, Player player, boolean notify)
    {
        if(check(permission, player.getUniqueId(), player.hasPermission("mychunks.server-admin")))
            return true;

        if(notify)
            permission.notifyFailure(player, owner);

        return false;
    }

    public boolean check(Permission permission, UUID playerUniqueId)
    {
        return check(permission, playerUniqueId, PlayerName.ADMINS.equalsPlayer(playerUniqueId));
    }

    public boolean check(Permission permission, UUID playerUniqueId, boolean isAdmin)
    {
        if(owner.getUniqueId().equals(playerUniqueId))
            return true;

        if(owner.equalsPlayer(PlayerName.ADMINS) && isAdmin)
            return true;

        Set<Member> memberSet= members.get(playerUniqueId);
        if(memberSet != null)
            for(Member member: memberSet)
                if(member != null && member.getRank().getPermission(permission).orElse(false))
                    return true;

        if(getPublicPermission(permission).orElse(false))
            return true;

        Zone zone = this.zone;
        return zone != null && zone.check(permission, playerUniqueId);
    }

    /**
     * The world where the chunk is located
     */
    public UUID getWorldId()
    {
        return worldId;
    }

    /**
     * The chunk position in the world
     */
    public Vector3i getPosition()
    {
        return position;
    }

    /**
     * The owner of this chunk, note that it can also be a fake player like {@link PlayerName#ADMINS}.
     */
    public PlayerName getOwner()
    {
        return owner;
    }

    /**
     * Changes the owner of this chunk, the change is not persisted immediately
     * @param owner The new owner, can be a fake player but can't be null
     */
    public void setOwner(PlayerName owner)
    {
        this.owner = owner;
    }

    /**
     * The zone that this chunk resides, can be {@code null} if this chunk is not part of a zone.
     */
    @Nullable
    public Zone getZone()
    {
        return zone;
    }

    /**
     * <p>Marks this chunk as part of a zone, the chunk must be already defined on the zone.</p>
     * <p>A {@code null} value will mark this chunk as a protected chunk without zone and will <strong>not</strong> remove itself from the zone automatically</p>
     * @param zone The new zone or {@code null}
     * @throws IllegalArgumentException
     */
    void setZone(@Nullable Zone zone) throws IllegalArgumentException
    {
        if(zone != null && zone.getChunkAt(position).orElse(null) != this)
            throw new IllegalArgumentException("The zone "+zone.getName()+" does not contains this chunk "+position);

        this.zone = zone;
    }

    public void addMember(Member member)
    {
        UUID playerId = member.getPlayerId().getUniqueId();
        Set<Member> memberSet = members.get(playerId);
        if(memberSet == null) members.put(playerId, memberSet = new HashSet<>(1));
        memberSet.add(member);
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

        return modified;
    }

    public boolean setPublicPermission(Permission permission, Tristate value)
    {
        if(value == Tristate.UNDEFINED)
            return publicPermissions.remove(permission) != null;

        boolean bool = value.asBoolean();
        Boolean replacement = publicPermissions.put(permission, bool);
        return replacement == null || bool != replacement;
    }
}
