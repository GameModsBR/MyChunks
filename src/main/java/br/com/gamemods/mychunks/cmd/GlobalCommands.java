package br.com.gamemods.mychunks.cmd;

import br.com.gamemods.mychunks.MyChunks;
import br.com.gamemods.mychunks.data.state.ClaimedChunk;
import br.com.gamemods.mychunks.data.state.PlayerName;
import br.com.gamemods.mychunks.data.state.WorldFallbackContext;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

import static br.com.gamemods.mychunks.Util.blockToChunk;

/**
 * Commands that are available for everyone
 */
public class GlobalCommands
{
    private final MyChunks plugin;

    public GlobalCommands(MyChunks plugin)
    {
        this.plugin = plugin;
    }

    public CommandResult map(CommandSource src, CommandContext args) throws CommandException
    {
        //TODO Implement
        src.sendMessage(Text.of("Unsupported"));
        return CommandResult.empty();
    }

    public CommandResult claim(CommandSource src, CommandContext commandContext)
    {
        Player player = (Player) src;
        Location<World> location = player.getLocation();
        UUID worldId = location.getExtent().getUniqueId();
        Vector3i chunkPosition = blockToChunk(location.getPosition().toInt());

        Optional<WorldFallbackContext> opt = plugin.getWorldContext(worldId);
        if(opt.isPresent())
        {
            String reason = "Failed to load the world context for the world "+location.getExtent().getName();
            player.sendMessage(Text.builder(reason).color(TextColors.RED).build());
            return CommandResult.empty();
        }

        WorldFallbackContext worldContext = opt.get();

        ClaimedChunk claimedChunk = new ClaimedChunk(worldContext, chunkPosition);
        claimedChunk.setOwner(new PlayerName(player.getUniqueId(), player.getName()));
        plugin.getChunkMap(worldId).get().put(chunkPosition, claimedChunk);
        player.sendMessage(Text.of("The chunk "+chunkPosition+" is now protected"));
        return CommandResult.success();
    }
}
