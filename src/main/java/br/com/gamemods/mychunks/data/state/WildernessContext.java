package br.com.gamemods.mychunks.data.state;

import org.spongepowered.api.entity.living.player.Player;

import java.util.EnumMap;

public class WildernessContext extends PublicContext
{
    public WildernessContext()
    {
    }

    public WildernessContext(EnumMap<Permission, Boolean> publicPermissions)
    {
        super(publicPermissions);
    }

    @Override
    protected boolean getDefaultPublicPermission(Permission permission)
    {
        return permission.isAllowedByDefaultOnTheWild();
    }

    @Override
    public void notifyFailure(Permission permission, Player player)
    {
        permission.notifyFailure(player, PlayerName.WILDERNESS);
    }
}
