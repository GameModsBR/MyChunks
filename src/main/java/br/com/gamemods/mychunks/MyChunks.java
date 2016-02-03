package br.com.gamemods.mychunks;

import static br.com.gamemods.mychunks.Util.*;

import br.com.gamemods.mychunks.data.api.DataStorage;
import br.com.gamemods.mychunks.data.api.DataStorageException;
import br.com.gamemods.mychunks.data.state.ClaimedChunk;
import br.com.gamemods.mychunks.data.state.Permission;
import br.com.gamemods.mychunks.data.state.PlayerName;
import com.flowpowered.math.vector.Vector3i;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.SaveWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.event.world.chunk.UnloadChunkEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Plugin(id="MyChunks", name = "MyChunks", version = "1.0-SNAPSHOT")
public class MyChunks
{
    private Map<UUID, Map<Vector3i, ClaimedChunk>> claimedChunks = new ConcurrentHashMap<>(1);
    private DataStorage dataStorage = (x,y) -> Optional.empty();

    @Inject
    private Logger logger;

    @Listener
    public void onServerStarting(GameStartingServerEvent event)
    {
        CommandSpec spec = CommandSpec.builder()
                .description(Text.of("Claims the chunk that you standing"))
                .executor((src, args) -> {
                    Player player = (Player) src;
                    Location<World> location = player.getLocation();
                    UUID worldId = location.getExtent().getUniqueId();
                    Vector3i chunkPosition = blockToChunk(location.getPosition().toInt());
                    ClaimedChunk claimedChunk = new ClaimedChunk(worldId, chunkPosition);
                    claimedChunk.setOwner(new PlayerName(player.getUniqueId(), player.getName()));
                    getChunkMap(worldId).get().put(chunkPosition, claimedChunk);
                    player.sendMessage(Text.of("The chunk "+chunkPosition+" is now protected"));
                    return CommandResult.success();
                })
                .build()
        ;

        Sponge.getCommandManager().register(this, spec, "claim");
    }

    @Listener
    public void onModifyBlock(ChangeBlockEvent event, @First Player player)
    {
        logger.info("Call: "+event.getClass());
        if(event.getCause().get(NamedCause.NOTIFIER, Player.class).map(p -> p.equals(player)).orElse(false))
        {
            logger.info("Player cause is notifier.");
            return;
        }

        getChunkMap(event.getTargetWorld()).ifPresent(subMap -> {
            Set<Vector3i> checkedChunks = new HashSet<>(2);

            for(Transaction<BlockSnapshot> transaction: event.getTransactions())
            {
                Vector3i chunkPosition = blockToChunk(transaction.getOriginal().getPosition());
                if (checkedChunks.contains(chunkPosition))
                    continue;

                ClaimedChunk claimedChunk = subMap.get(chunkPosition);
                if (claimedChunk != null && !claimedChunk.check(Permission.MODIFY, player))
                {
                    logger.info("Chunk modification cancelled: "+chunkPosition+" "+event.getCause());
                    event.setCancelled(true);
                    return;
                }

                checkedChunks.add(chunkPosition);
                logger.info("Chunk modification allowed: "+chunkPosition+" "+event);
            }
        });
        logger.info("Return");
    }

    @Listener
    public void on(SaveWorldEvent event)
    {
        logger.info("World save: "+event.getTargetWorld().getName());
    }

    @Listener
    public void onWorldLoad(LoadWorldEvent event)
    {
        claimedChunks.put(event.getTargetWorld().getUniqueId(), new HashMap<>());
        logger.info("World loaded: "+event.getTargetWorld().getName());
    }

    @Listener
    public void onWorldUnload(UnloadWorldEvent event)
    {
        claimedChunks.remove(event.getTargetWorld().getUniqueId());
        logger.info("World unloaded: "+event.getTargetWorld().getName());
    }

    public Optional<ClaimedChunk> getChunkData(Chunk chunk)
    {
        return getChunkMap(chunk.getWorld())
                .map(subMap -> subMap.get(chunk.getPosition()));
    }

    private Optional<Map<Vector3i, ClaimedChunk>> getChunkMap(World world)
    {
        return getChunkMap(world.getUniqueId());
    }

    private Optional<Map<Vector3i, ClaimedChunk>> getChunkMap(UUID worldId)
    {
        Map<Vector3i, ClaimedChunk> subMap = claimedChunks.get(worldId);
        if(subMap == null)
            return Optional.empty();
        return Optional.of(subMap);
    }

    @Listener
    public void onChunkLoad(LoadChunkEvent event)
    {
        Chunk chunk = event.getTargetChunk();
        UUID worldId = chunk.getWorld().getUniqueId();
        Vector3i position = chunk.getPosition();

        try
        {
            dataStorage.loadChunk(worldId, position).ifPresent(claimedChunk ->{
                logger.info("Chunk loaded: "+chunk.getWorld().getName()+position);
                getChunkMap(worldId).orElseThrow(IllegalStateException::new).put(position, claimedChunk);
            });
        } catch (DataStorageException e)
        {
            logger.error("Failed to load chunk information on "+chunk.getWorld().getName()+position, e);
            getChunkMap(worldId).orElseThrow(IllegalStateException::new).put(position, new ClaimedChunk(worldId, position));
        }
    }

    @Listener
    public void onChunkUnload(UnloadChunkEvent event)
    {
        Chunk chunk = event.getTargetChunk();
        Vector3i position = chunk.getPosition();
        getChunkMap(chunk.getWorld()).ifPresent(chunkMap -> {
            if(chunkMap.remove(position) != null)
                logger.info("Chunk unloaded: "+chunk.getWorld().getName()+position);
        } );
    }
}
