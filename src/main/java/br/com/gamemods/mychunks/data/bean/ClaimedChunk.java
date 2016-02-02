package br.com.gamemods.mychunks.data.bean;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.entity.living.player.Player;

import java.util.UUID;

public class ClaimedChunk
{
    private final UUID worldId;
    private final Vector3i position;

    public ClaimedChunk(UUID worldId, Vector3i position)
    {
        this.worldId = worldId;
        this.position = position;
    }

    public boolean checkModify(Player player)
    {
        return false;
    }
}
