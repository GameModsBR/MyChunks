package br.com.gamemods.mychunks.data.state;

import br.com.gamemods.mychunks.Util;
import org.spongepowered.api.util.Identifiable;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

/**
 * A rank grants permissions extra permissions on contexts, all ranks must be unique on context bases and
 * the uniqueness must be checked with {@link #normalizedName()}. It also contains an UUID.
 */
@ParametersAreNonnullByDefault
@NonnullByDefault
public class Rank implements Identifiable, Modifiable
{
    private final UUID rankId;
    private String name = "unnamed";
    private EnumSet<Permission> permissions;
    private boolean modified;

    public Rank(String name, EnumSet<Permission> permissions)
    {
        rankId = UUID.randomUUID();
        setName(name);
        this.permissions = permissions;
    }

    public Rank(String name)
    {
        rankId = UUID.randomUUID();
        setName(name);
        this.permissions = EnumSet.noneOf(Permission.class);
    }

    /**
     * Checks if this rank grants an specific permission.
     * @param permission The permission to be checked
     * @return {@code true} if the permission is granted, {@code false} if it's revoked or empty if it's not defined.<br>
     * Note that this method will never return {@code false} currently but this feature may be implemented in future.
     */
    public Optional<Boolean> getPermission(Permission permission)
    {
        return permissions.contains(permission)? Optional.of(true) : Optional.empty();
    }

    /**
     * The human readable name of this rank
     */
    public String getName()
    {
        return name;
    }

    /**
     * Changes the name of this rank. Note that this won't update any reference to the name automatically and will not check for conflicts.
     * @param name The new rank name, cannot be empty.
     * @throws IllegalArgumentException if the name is empty
     */
    public void setName(String name) throws IllegalArgumentException
    {
        name = name.trim();
        if(name.isEmpty())
            throw new IllegalArgumentException("Name is empty");

        modified |= !this.name.equals(name);
        this.name = name;
    }

    /**
     * The normalized name for conflict checks
     * @see Util#normalizeIdentifier(String)
     */
    public String normalizedName()
    {
        return Util.normalizeIdentifier(name);
    }

    @Override
    public UUID getUniqueId()
    {
        return rankId;
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
