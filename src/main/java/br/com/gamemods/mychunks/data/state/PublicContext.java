package br.com.gamemods.mychunks.data.state;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Tristate;

import java.util.EnumMap;
import java.util.Optional;
import java.util.UUID;

public class PublicContext implements Modifiable
{
    private EnumMap<Permission, Boolean> publicPermissions;
    protected boolean modified;

    public PublicContext()
    {
        publicPermissions = new EnumMap<>(Permission.class);
    }

    public PublicContext(EnumMap<Permission, Boolean> publicPermissions)
    {
        this.publicPermissions = publicPermissions;
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
        if(check(permission, player.getUniqueId(), isAdmin(player)))
            return true;

        if(notify)
            notifyFailure(permission, player);

        return false;
    }

    public boolean isAdmin(Player player)
    {
        return player.hasPermission("mychunks.server-admin");
    }

    public void notifyFailure(Permission permission, Player player)
    {
        permission.notifyFailure(player, null);
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
        Optional<Boolean> result = getPermission(permission, playerUniqueId, isAdmin);
        if(result.isPresent())
            return result.get();

        return getPublicPermission(permission).orElse(getDefaultPublicPermission(permission));
    }

    /**
     * The
     * @param permission
     * @return
     */
    protected boolean getDefaultPublicPermission(Permission permission)
    {
        return false;
    }

    /**
     * Checks if the player have a custom permission on this context that overrides the public permissions.
     * @param permission The permission to be checked
     * @param playerUniqueId The player that needs this permission
     * @param isAdmin If the player is an adminstrator
     * @return An optional wrapper that may contains the custom permission. If it's empty the player may still be
     * allowed by the result of the {@link #getPublicPermission(Permission)}
     */
    public Optional<Boolean> getPermission(Permission permission, UUID playerUniqueId, boolean isAdmin)
    {
        return Optional.empty();
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

    public boolean setPublicPermission(Permission permission, Tristate value)
    {
        if(value == Tristate.UNDEFINED)
            return publicPermissions.remove(permission) != null;

        boolean bool = value.asBoolean();
        Boolean replacement = publicPermissions.put(permission, bool);
        bool = replacement == null || bool != replacement;

        modified |= bool;
        return bool;
    }

    @Override
    public boolean isModified()
    {
        return modified;
    }

    @Override
    public void setModified(boolean modified)
    {
        this.modified = modified;
    }
}
