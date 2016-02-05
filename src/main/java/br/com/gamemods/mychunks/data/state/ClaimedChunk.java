package br.com.gamemods.mychunks.data.state;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * A chunk that is protected on the server, it can be claimed to a player or to the server admins.
 */
@ParametersAreNonnullByDefault
@NonnullByDefault
public class ClaimedChunk extends OwnedContext
{
    private final WorldFallbackContext worldContext;
    private final Vector3i position;

    @Nullable
    private Zone zone;

    /**
     * Construct a chunk that is claimed by the server admins, with no members and no public permission specified
     * @param worldContext The world that this chunk resides
     * @param position The chunk position
     */
    public ClaimedChunk(WorldFallbackContext worldContext, Vector3i position)
    {
        this.worldContext = worldContext;
        this.position = position;
    }

    @Override
    public Optional<Boolean> getPermission(Permission permission, UUID playerUniqueId, boolean isAdmin)
    {
        Optional<Boolean> result = super.getPermission(permission, playerUniqueId, isAdmin);
        if(result.isPresent())
            return result;

        Zone zone = this.zone;
        if(zone == null)
            return result;

        /*
         * There are two way to check the zone
         * 1: The chunk owner is the same as the zone owner and the chunk does not have any special member added to it
         * 2: The chunk owner is different from the zone owner or has special permissions
         *
         * Assumptions:
         * - If the player is not the owner of the chunk so he's not the owner of the zone
         *
         * If the player does not a special rank on the chunk that grants this permission we need to check if this
         * chunk is fully integrated to the zone or not, if it is we need to check the permission directly on the zone
         */
        if(isIntegratedToTheZone())
            return zone.getPermission(permission, playerUniqueId, isAdmin);

        // The public zone public permission is not the same as the chunk public permission
        //TODO Should it be on the getPublicPermission() of this context or not?
        return zone.getPublicPermission(permission);
    }

    public boolean isIntegratedToTheZone()
    {
        return zone != null && getMembers().isEmpty() && getOwner().equals(zone.getOwner());
    }

    /**
     * The world where the chunk is located
     */
    public UUID getWorldId()
    {
        return worldContext.getWorldId();
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

        modified |= !Objects.equals(this.zone, zone);
        this.zone = zone;
    }

    public WorldFallbackContext getWorldContext()
    {
        return worldContext;
    }
}
