package br.com.gamemods.mychunks.data.api;

import br.com.gamemods.mychunks.data.state.ClaimedChunk;
import com.flowpowered.math.vector.Vector3i;

import java.util.Optional;
import java.util.UUID;

public interface DataStorage
{
    Optional<ClaimedChunk> loadChunk(UUID worldId, Vector3i position);
}
