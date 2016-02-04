package br.com.gamemods.mychunks.data.binary;

import br.com.gamemods.mychunks.data.api.DataStorage;
import br.com.gamemods.mychunks.data.api.DataStorageException;
import br.com.gamemods.mychunks.data.state.ClaimedChunk;
import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static br.com.gamemods.mychunks.Util.chunkToRegion;

/**
 * Data storage implementation that stores the data as binary files, on the disk.
 * <p>The data is saved in multiple files organized by directories</p>
 */
@NonnullByDefault
@ParametersAreNonnullByDefault
public class BinaryDataStorage implements DataStorage
{
    private final File storageDir;

    // We cache the data to prevent issues with slow IO operations when the same chunk is loaded and unloaded too many times
    // and when multiple chunks are loaded from the same region too quickly
    private LoadingCache<UUID, WorldData> worldCache = CacheBuilder.newBuilder()
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, WorldData>()
            {
                @Override
                public WorldData load(UUID key) throws Exception
                {
                    return new WorldData(key);
                }
            });

    public BinaryDataStorage(File storageDir) throws IOException
    {
        this.storageDir = storageDir;
        if(!storageDir.isDirectory() && !storageDir.mkdirs())
            throw new IOException("Failed to create the directory "+storageDir);
    }

    @Override
    public Optional<ClaimedChunk> loadChunk(UUID worldId, Vector3i position) throws DataStorageException
    {
        try
        {
            return worldCache.get(worldId).getChunk(position);
        }
        catch (ExecutionException e)
        {
            throw new DataStorageException(e);
        }
    }

    @Override
    public void saveChunk(ClaimedChunk chunk)
    {

    }

    /**
     * A cache of all data read about a world
     */
    private class WorldData
    {
        private final UUID worldId;
        private final LoadingCache<Vector2i, RegionData> regionCache = CacheBuilder.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build(new CacheLoader<Vector2i, RegionData>()
                {
                    @Override
                    public RegionData load(Vector2i key) throws Exception
                    {
                        return new RegionData(key);
                    }
                });

        private WorldData(UUID worldId)
        {
            this.worldId = worldId;
        }

        private Optional<ClaimedChunk> getChunk(Vector3i position) throws ExecutionException
        {
            return Optional.ofNullable(regionCache.get(chunkToRegion(position)).claimedChunkMap.get(position));
        }

        /**
         * A cache of all chunks in a region file
         */
        private class RegionData
        {
            private final Vector2i position;
            private final Map<Vector3i, ClaimedChunk> claimedChunkMap = new HashMap<>(0);

            private RegionData(Vector2i position)
            {
                this.position = position;
            }
        }
    }
}
