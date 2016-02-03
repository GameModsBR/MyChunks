package br.com.gamemods.mychunks.data.state;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
@NonnullByDefault
public class PermissionContext
{
    private Optional<PlayerName> owner = Optional.empty();
    private Map<UUID, Set<Member>> members = new HashMap<>(0);
    private EnumMap<Permission, Boolean> publicPermissions;

    public PermissionContext()
    {
        publicPermissions = new EnumMap<>(Permission.class);
    }

    public PermissionContext(EnumMap<Permission, Boolean> publicPermissions)
    {
        this.publicPermissions = publicPermissions;
    }

    /**
     * <p>Checks if this context declares an specific permission as public.</p>
     * Public permissions allows anyone to do this action regardless if the subject is a member or not.
     * @param permission The permission to be checked
     * @return {@code true} if it's permitted, {@code false} if it's denied or empty if it's not defined.
     */
    public Optional<Boolean> getPublicPermission(Permission permission)
    {
        return Optional.ofNullable(publicPermissions.get(permission));
    }

    /**
     * <p>Checks if a player has an specific permission on this context.</p>
     * <p>It checks the player public permissions, the player rank and anything that is needed on this context.</p>
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
     * <p>Checks if a player has an specific permission on this context.</p>
     * <p>It checks the player public permissions, the player rank and anything that is needed on this context.</p>
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
            permission.notifyFailure(player, owner.orElse(PlayerName.ADMINS));

        return false;
    }

    public boolean check(Permission permission, PlayerName playerName, boolean isAdmin)
    {
        return check(permission, playerName.getUniqueId(), isAdmin);
    }

    public boolean check(Permission permission, PlayerName playerName)
    {
        return check(permission, playerName.getUniqueId(), false);
    }

    public boolean check(Permission permission, UUID playerUniqueId)
    {
        return check(permission, playerUniqueId, PlayerName.ADMINS.equalsPlayer(playerUniqueId));
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

        return getPublicPermission(permission).orElse(false);
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
        this.owner = Optional.ofNullable(owner);
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
