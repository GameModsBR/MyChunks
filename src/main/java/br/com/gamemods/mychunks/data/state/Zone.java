package br.com.gamemods.mychunks.data.state;

import br.com.gamemods.mychunks.Util;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.util.Identifiable;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.naming.InvalidNameException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * <p>A group of claimed chunks that shares the same fallback permissions</p>
 * <p>Although the name must be unique on the server the identification must be done based on an UUID, the reason is that
 * the name can be changed at any time and it can be overcomplicated to update all references</p>
 */
@ParametersAreNonnullByDefault
@NonnullByDefault
public class Zone extends OwnedContext implements Identifiable
{
    private final UUID zoneId;
    private final WorldFallbackContext worldContext;
    private String name = "unnamed";
    private Map<Vector3i, ClaimedChunk> chunkMap = new HashMap<>(1);

    /**
     * Construct a zone instance with a specified UUID, this constructor is normally used to load a persisted zone.
     * <p>It's safe to construct as no exception is thrown if the name is invalid, unless it's empty</p>
     * @param name The zone name. The only restriction is that it can't be empty. If the name is not compatible with the rules
     *             defined on {@link #setName(String)} then it will be automatically adapted without notification.
     * @throws IllegalArgumentException If the name is empty or contains only empty characters
     */
    public Zone(UUID zoneId, WorldFallbackContext worldContext, String name) throws IllegalArgumentException
    {
        this.zoneId = zoneId;
        this.worldContext = worldContext;
        setValidName(name);
    }

    public Zone(WorldFallbackContext worldContext, String name) throws InvalidNameException
    {
        this.zoneId = UUID.randomUUID();
        this.worldContext = worldContext;
        setName(name);
    }

    public Optional<ClaimedChunk> getChunkAt(Vector3i position)
    {
        return Optional.ofNullable(chunkMap.get(position));
    }

    public boolean isChunkRequired(final Vector3i position)
    {
        if(chunkMap.size() <= 2 || !chunkMap.containsKey(position))
            return false;

        lookup:
        for(Vector3i direction: Util.CARDINAL_DIRECTIONS)
        {
            Vector3i supported = position.add(direction);
            if(!chunkMap.containsKey(supported))
                continue;

            for(Vector3i secondDirection: Util.CARDINAL_DIRECTIONS)
            {
                Vector3i alternativeSupport = supported.add(secondDirection);
                if(alternativeSupport.equals(position))
                    continue;

                if(chunkMap.containsKey(alternativeSupport))
                    continue lookup;
            }

            return true;
        }

        return false;
    }

    public void removeChunkAt(Vector3i position) throws IllegalArgumentException
    {
        if(isChunkRequired(position))
            throw new IllegalArgumentException("The chunk "+position+" is required by an other chunk");

        ClaimedChunk removed = chunkMap.remove(position);
        if(removed != null)
        {
            removed.setZone(null);
            modified = true;
        }
    }

    public void addChunk(ClaimedChunk chunk) throws IllegalArgumentException
    {
        if(!chunk.getWorldId().equals(worldContext.getWorldId()))
            throw new IllegalArgumentException(
                    "The chunk "+chunk.getWorldId()+chunk.getPosition()+
                            " is not part of the world "+worldContext.getWorldId()
            );

        Zone zone = chunk.getZone();
        if(zone == this)
            return;

        Vector3i addedPosition = chunk.getPosition();
        if(zone != null)
            zone.removeChunkAt(addedPosition);

        check:
        if(!chunkMap.isEmpty())
        {
            chunkIteration:
            for(Vector3i position: chunkMap.keySet())
            {
                int[] ints = addedPosition.sub(position).toArray();
                boolean one = false;
                for(int i: ints)
                    if(i == 1 || i == -1)
                    {
                        // Diagonal
                        if(one) continue chunkIteration;
                            // Cross
                        else one = true;
                    }

                if(one)
                    break check;
            }

            throw new IllegalArgumentException("The chunk "+addedPosition+" is not touching any of these chunks: "+chunkMap.keySet());
        }

        chunkMap.put(addedPosition, chunk);
        chunk.setZone(this);
        modified = true;
    }

    public UUID getWorldId()
    {
        return worldContext.getWorldId();
    }

    public String getName()
    {
        return name;
    }

    public String normalizedName()
    {
        return Util.normalizeIdentifier(name);
    }

    /**
     * Changes the zone name doing any necessary modification to the name be a valid name
     * @param name The requested name, can't be empty.
     * @return The name that was actually set.
     * @throws IllegalArgumentException If the name is empty or contains only empty characters
     */
    public String setValidName(String name) throws IllegalArgumentException
    {
        name = name.trim();
        if(name.isEmpty())
            throw new IllegalArgumentException("Name is empty");

        String normalized = Util.normalizeIdentifier(name);
        if(normalized.isEmpty()) name = "Unnamed Zone "+System.currentTimeMillis();

        modified |= !this.name.equals(name);
        this.name = name;
        return name;
    }

    public void setName(String name) throws InvalidNameException
    {
        if(!name.trim().equals(name)) throw new InvalidNameException("Name has trailing or leading whitespace");
        String normalized = Util.normalizeIdentifier(name);
        if(normalized.isEmpty()) throw new InvalidNameException("Normalized name is empty");

        modified |= !this.name.equals(name);
        this.name = name;
    }

    public UUID getUniqueId()
    {
        return zoneId;
    }

    @Override
    public void setOwner(@Nullable PlayerName owner) throws UnsupportedOperationException
    {
        chunkMap.values().stream().filter(ClaimedChunk::isIntegratedToTheZone).forEach(chunk -> chunk.setOwner(owner));
        super.setOwner(owner);
    }
}
