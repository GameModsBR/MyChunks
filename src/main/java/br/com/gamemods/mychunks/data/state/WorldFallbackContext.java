package br.com.gamemods.mychunks.data.state;

import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@NonnullByDefault
@ParametersAreNonnullByDefault
public class WorldFallbackContext extends PublicContext
{
    private final UUID worldId;
    private WildernessContext wilderness;

    public WorldFallbackContext(UUID worldId)
    {
        this.worldId = worldId;
        wilderness = new WildernessContext();
    }

    public WildernessContext getWilderness()
    {
        return wilderness;
    }

    public UUID getWorldId()
    {
        return worldId;
    }

    @Override
    protected boolean getDefaultPublicPermission(Permission permission)
    {
        return permission.isAllowedByDefault();
    }
}
