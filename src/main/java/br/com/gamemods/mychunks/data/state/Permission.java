package br.com.gamemods.mychunks.data.state;

import br.com.gamemods.mychunks.Util;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

@ParametersAreNonnullByDefault
public enum Permission
{
    MODIFY("You do not have permission to modify this chunk.")

    ;

    private static EnumSet<Permission> defaultPermissions = EnumSet.noneOf(Permission.class);

    public static EnumSet<Permission> getDefaultPermissions()
    {
        return EnumSet.copyOf(defaultPermissions);
    }

    public static EnumSet<Permission> enumSet(Collection<Permission> collection)
    {
        return Util.enumSet(Permission.class, collection);
    }

    private boolean defaultValue = false;
    private Text failureMessage;

    Permission(String failureMessage)
    {
        this.failureMessage = Text.builder(failureMessage).color(TextColors.RED).build();
    }

    Permission(boolean defaultValue, Text failureMessage)
    {
        setAllowedByDefault(defaultValue);
        this.failureMessage = failureMessage;
    }

    public boolean isAllowedByDefault()
    {
        return defaultValue;
    }

    public void setAllowedByDefault(boolean allowed)
    {
        this.defaultValue = allowed;
        if(allowed)
            defaultPermissions.add(this);
        else
            defaultPermissions.remove(this);
    }

    public void notifyFailure(Player player, @Nullable PlayerName owner)
    {
        Text.Builder message = header().color(TextColors.DARK_RED).append(failureMessage);
        if(owner != null)
            message.append(Text.NEW_LINE)
                .append(Text.builder("Owner: ").color(TextColors.DARK_RED).build())
                .append(Text.builder(owner.getName()).color(TextColors.GOLD).build());

        player.sendMessage(message.build());
    }

    private static Text.Builder header()
    {
        return Text.builder("Permission> ");
    }
}
