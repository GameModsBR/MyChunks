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
public class ClaimedChunk extends PermissionContext
{
    private final UUID worldId;
    private final Vector3i position;

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
    }

    /**
     * <p>Checks if a player has an specific permission on this context.</p>
     * <p>It checks the player public permissions, the player rank and the zone that this chunk resides.</p>
     * <p>The player will be notified if the permission is denied</p>
     * @param permission The permission to be checked
     * @param playerUniqueId The player that needs this permission
     * @param isAdmin If the player has admin permission
     * @return If the player has permission
     */
    @Override
    public boolean check(Permission permission, UUID playerUniqueId, boolean isAdmin)
    {
        if(super.check(permission, playerUniqueId, isAdmin))
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
}
