package br.com.gamemods.mychunks;

import br.com.gamemods.mychunks.cmd.GlobalCommands;
import br.com.gamemods.mychunks.data.api.DataStorage;
import br.com.gamemods.mychunks.data.api.DataStorageException;
import br.com.gamemods.mychunks.data.binary.BinaryDataStorage;
import br.com.gamemods.mychunks.data.state.*;
import com.flowpowered.math.vector.Vector3i;
import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.SaveWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.event.world.chunk.UnloadChunkEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static br.com.gamemods.mychunks.Util.blockToChunk;
import static br.com.gamemods.mychunks.data.state.Permission.MODIFY;
import static br.com.gamemods.mychunks.data.state.Permission.values;

@Plugin(id="MyChunks", name = "MyChunks", version = "1.0-SNAPSHOT")
public class MyChunks
{
    private Map<UUID, Map<Vector3i, ClaimedChunk>> claimedChunks = new ConcurrentHashMap<>(1);
    private Map<UUID, WorldFallbackContext> worldContexts = new ConcurrentHashMap<>(1);
    private DataStorage dataStorage;

    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> mainConfigLoader;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    @Listener
    public void onGameInit(GameInitializationEvent event)
    {
        ConfigurationOptions options = ConfigurationOptions.defaults().setShouldCopyDefaults(true);
        try
        {
            HoconConfigurationLoader defaultConfigLoader = HoconConfigurationLoader
                    .builder().setPath(configDir.resolve("default-permissions.conf")).build();

            CommentedConfigurationNode defaultPermissions = defaultConfigLoader.load(options);

            CommentedConfigurationNode fallback = defaultPermissions.getNode("fallback");
            fallback.setComment("Defines the fallback values for permission flags that are not defined on any context of a protected terrain.");

            CommentedConfigurationNode wild = defaultPermissions.getNode("default-world-permissions");
            wild.setComment("The default permissions for new worlds/dimensions that affects unclaimed chunks");

            loadWorldConfig(null, wild, true);
            loadWorldConfig(null, fallback, false);

            try
            {
                defaultConfigLoader.save(defaultPermissions);
            }
            catch (IOException e)
            {
                logger.error("Failed to save the default-permissions.config file", e);
            }
        }
        catch (IOException e)
        {
            logger.error("Failed to load the default-permissions.config file", e);
            Sponge.getServer().shutdown();
        }


        try
        {
            CommentedConfigurationNode mainConfig = mainConfigLoader.load(options);
            mainConfig.setComment("This configuration defines the fundamental settings from MyChunks");

            CommentedConfigurationNode dataStorageNode = mainConfig.getNode("data-storage");
            CommentedConfigurationNode engineNode = dataStorageNode.getNode("engine");
            engineNode.setComment("The storage engine can only be \"binary\" currently");

            CommentedConfigurationNode binaryNode = dataStorageNode.getNode("binary");
            binaryNode.setComment("Configurations used by the binary engine.");

            CommentedConfigurationNode node = binaryNode.getNode("save-dir");
            node.setComment("The directory where the binary data will be saved. It must be writable");
            String binarySaveDir = node.getString(configDir.resolve("data").resolve("binary").toString());

            String engine = engineNode.getString("binary");
            if("binary".equalsIgnoreCase(engine.trim()))
                dataStorage = new BinaryDataStorage(Paths.get(binarySaveDir).toFile());
            else
                throw new IllegalArgumentException("The storage engine '"+engine+"' is not supported");

            try
            {
                mainConfigLoader.save(mainConfig);
            }
            catch (IOException e)
            {
                logger.error("Failed to save the main config file", e);
            }

        } catch (Exception e)
        {
            logger.error("Failed to load the main config file", e);
            Sponge.getServer().shutdown();
        }
    }

    private void loadWorldConfig(@Nullable PublicContext context, CommentedConfigurationNode parentNode, boolean wild)
    {
        for(Permission permission : values())
        {
            CommentedConfigurationNode node = parentNode.getNode(permission.toString().toLowerCase());
            boolean def = wild? permission.isAllowedByDefaultOnTheWild() : permission.isAllowedByDefault();
            node.setComment(permission.getDescription() + " [Default:" + def + "]");

            if(context != null)
            {
                if(!node.isVirtual())
                {
                    Object value = node.getValue();
                    if(value instanceof Boolean)
                    {
                        context.setPublicPermission(permission, Tristate.fromBoolean((boolean) value));
                        context.setModified(false);
                    }
                }
            }
            else
            {
                boolean val = node.getBoolean(def);
                if (val != def)
                {
                    if (wild)
                        permission.setAllowedByDefaultOnTheWild(val);
                    else
                        permission.setAllowedByDefault(val);
                    permission.setModified(false);
                }
            }
        }
    }

    private WorldFallbackContext loadWorldContext(World world) throws IOException
    {
        Path worldPath = configDir.resolve("world");
        Path configPath = worldPath.resolve(world.getName().replaceAll("[^a-zA-Z0-9-]", "_") + ".conf");
        if(!Files.isDirectory(worldPath))
            Files.createDirectory(worldPath);

        HoconConfigurationLoader loader = HoconConfigurationLoader.builder().setPath(configPath).build();
        CommentedConfigurationNode main = loader.load(
                ConfigurationOptions.defaults()
                        .setShouldCopyDefaults(true)
                        .setHeader("Default wild permissions on the world " + world.getName() + " with dimension " +
                                world.getDimension().getName() + " and unique id: " + world.getUniqueId())
        );

        CommentedConfigurationNode wildPerms = main.getNode("wild-permissions");
        wildPerms.setComment(
                "The permissions that affects unclaimed chunks on this world. Null values are inherited from the default-world-permissions declared on the default-permissions.conf file"
        );

        CommentedConfigurationNode defaultPerms = main.getNode("fallback-permissions");
        defaultPerms.setComment(
                "The fallback permissions that will be used when a claimed chunk or a zone does not define the permission, this will take priority over the default defined on the defailt-permission.conf file"
        );

        WorldFallbackContext worldContext = new WorldFallbackContext(world.getUniqueId());
        loadWorldConfig(worldContext, defaultPerms, false);
        loadWorldConfig(worldContext.getWilderness(), wildPerms, true);

        try
        {
            loader.save(main);
        }
        catch(Exception e)
        {
            logger.error("Failed to save the world config file for " + world.getName() + " DIM:" +
                    world.getDimension().getName(), e);
        }

        return worldContext;
    }

    @Listener
    public void onServerStarting(GameStartingServerEvent event)
    {
        GlobalCommands globalCommands = new GlobalCommands(this);
        CommandSpec map = CommandSpec.builder()
            .description(Text.of("All commands related to the chunk protection"))
            .executor(globalCommands::map)
            .build();

        CommandSpec claim = CommandSpec.builder()
                .description(Text.of("Claims the chunk that you are standing"))
                .executor(globalCommands::claim)
                .build();

        CommandSpec chunk = CommandSpec.builder()
                .description(Text.of("Commands related to chunk protection"))
                .child(map, "map")
                .child(claim, "claim")
                .build();

        CommandSpec mychunk = CommandSpec.builder()
                .description(Text.of("All mychunk commands"))
                .child(chunk, "chunk", "c")
                .build();

        Sponge.getCommandManager().register(this, chunk, "chunk");
        Sponge.getCommandManager().register(this, mychunk, "mychunk");
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

        WorldFallbackContext worldContext = worldContexts.get(event.getTargetWorld().getUniqueId());

        boolean canModifyWild = Optional.ofNullable(worldContext)
                .flatMap(w->w.getWilderness().getPublicPermission(MODIFY)).orElse(MODIFY.isAllowedByDefaultOnTheWild());

        Optional<Map<Vector3i, ClaimedChunk>> chunkMap = getChunkMap(event.getTargetWorld());

        claimedCheck:
        if(chunkMap.isPresent())
        {
            Map<Vector3i, ClaimedChunk> subMap = chunkMap.get();
            Set<Vector3i> checkedChunks = new HashSet<>(2);

            for(Transaction<BlockSnapshot> transaction: event.getTransactions())
            {
                Vector3i chunkPosition = blockToChunk(transaction.getOriginal().getPosition());
                if (checkedChunks.contains(chunkPosition))
                    continue;

                ClaimedChunk claimedChunk = subMap.get(chunkPosition);
                if (claimedChunk != null && !claimedChunk.check(MODIFY, player))
                {
                    logger.info("Chunk modification cancelled: "+chunkPosition+" "+event.getCause());
                    event.setCancelled(true);
                    return;
                }
                else if(!canModifyWild)
                    break claimedCheck;

                checkedChunks.add(chunkPosition);
                logger.info("Chunk modification allowed: "+chunkPosition+" "+event);
            }

            return;
        }

        if(!canModifyWild)
        {
            if(worldContext != null)
                worldContext.getWilderness().notifyFailure(MODIFY, player);
            else
                MODIFY.notifyFailure(player,PlayerName.WILDERNESS);

            logger.info("Chunk modification cancelled because MODIFY is not allowed on unclaimed chunks");
            event.setCancelled(true);
        }
    }

    @Listener
    public void on(SaveWorldEvent event)
    {
        logger.info("World save: "+event.getTargetWorld().getName());
    }

    @Listener
    public void onWorldLoad(LoadWorldEvent event)
    {
        World world = event.getTargetWorld();
        logger.info("World loaded: "+ world.getName());
        UUID uniqueId = world.getUniqueId();
        claimedChunks.put(uniqueId, new HashMap<>());
        try
        {
            worldContexts.put(uniqueId, loadWorldContext(world));
        }
        catch (IOException e)
        {
            logger.error("Failed to load world data");
            Sponge.getServer().shutdown();
        }
    }

    @Listener
    public void onWorldUnload(UnloadWorldEvent event)
    {
        logger.info("World unloaded: "+event.getTargetWorld().getName());
        claimedChunks.remove(event.getTargetWorld().getUniqueId());
        worldContexts.remove(event.getTargetWorld().getUniqueId());
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

    public Optional<Map<Vector3i, ClaimedChunk>> getChunkMap(UUID worldId)
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

        WorldFallbackContext context = worldContexts.get(worldId);
        if(context == null)
        {
            IllegalStateException exception = new IllegalStateException(
                    "Failed to find the world context for the world "+chunk.getWorld().getName()
            );
            logger.error("An error happened while loading the chunk "+chunk.getWorld().getName()+position, exception);
            Sponge.getServer().shutdown();
            throw exception;
        }

        try
        {
            dataStorage.loadChunk(worldId, position).ifPresent(claimedChunk ->{
                logger.info("Chunk loaded: "+chunk.getWorld().getName()+position);
                getChunkMap(worldId).orElseThrow(IllegalStateException::new).put(position, claimedChunk);
            });
        } catch (DataStorageException e)
        {
            logger.error("Failed to load chunk information on "+chunk.getWorld().getName()+position, e);
            getChunkMap(worldId).orElseThrow(IllegalStateException::new).put(position, new ClaimedChunk(context, position));
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

    public Optional<WorldFallbackContext> getWorldContext(UUID worldId)
    {
        return Optional.ofNullable(worldContexts.get(worldId));
    }
}
