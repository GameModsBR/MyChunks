package br.com.gamemods.mychunks.data.state;

import br.com.gamemods.mychunks.Util;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.EnumSet;

/**
 * The permission flags that are used on protections
 */
@ParametersAreNonnullByDefault
@NonnullByDefault
public enum Permission implements Modifiable
{
    /**
     * Place, break or replace blocks
     */
    MODIFY("Allows to place/break/change blocks in the protected zone", "You do not have permission to modify this chunk."),
    ENTER(true, "Allows to be inside the protected zone", "You do not have permission to enter on this chunk.")

    ;

    private static final EnumSet<Permission> defaultPermissions = EnumSet.allOf(Permission.class);
    private static final EnumSet<Permission> defaultWildPermissions = EnumSet.allOf(Permission.class);
    static
    {
        defaultPermissions.removeIf(permission -> !permission.fallbackValue);
        defaultWildPermissions.removeIf(permission -> !permission.defaultWildValue);
    }

    /**
     * The permissions that are allowed by default. Changes tn the flags doesn't change sets returned prior the change
     * @return A new set with the current allowed permissions. It can be safely modified, the changes will not change
     * the default value on the flags and will not affect future calls to this method.
     */
    public static EnumSet<Permission> getDefaultPermissions()
    {
        return EnumSet.copyOf(defaultPermissions);
    }

    public static EnumSet<Permission> getDefaultWildPermissions()
    {
        return EnumSet.copyOf(defaultWildPermissions);
    }

    /**
     * Creates an {@link EnumSet} from a collection of this type.
     * @param collection Can be empty or {@code null}
     * @return A new set with all values added from the passed collection
     */
    public static EnumSet<Permission> enumSet(@Nullable Collection<Permission> collection)
    {
        return Util.enumSet(Permission.class, collection);
    }

    private boolean fallbackValue = false;
    private boolean defaultWildValue = false;
    private Text failureMessage;
    private String description;
    private boolean modified;

    Permission(String description, String failureMessage)
    {
        this(false, true, description, failureMessage);
    }

    Permission(boolean def, String description, String failureMessage)
    {
        this(def, def, description, failureMessage);
    }

    Permission(boolean def, boolean wild, String description, String failureMessage)
    {
        LiteralText.Builder builder = Text.builder(failureMessage);
        TextColor color = TextColors.RED;

        //noinspection ConstantConditions The colors are null on tests
        if(color != null)
            builder.color(color);

        this.description = description;
        this.failureMessage = builder.build();
        this.fallbackValue = def;
        this.defaultWildValue = wild;
        modified = false;
    }

    /**
     * <p>If this permission must be granted if it's not declared anywhere on the context.</p>
     * This must be used as fallback check and the value returned must not be saved with the context because it
     * can be changed anytime by the server administrators.
     */
    public boolean isAllowedByDefault()
    {
        return fallbackValue;
    }

    /**
     * Changes the fallback result for this permission, this will also add or remove this permission from sets returned by {@link #getDefaultPermissions()}
     */
    public void setAllowedByDefault(boolean allowed)
    {
        modified |= fallbackValue != allowed;
        this.fallbackValue = allowed;
        if(allowed)
            defaultPermissions.add(this);
        else
            defaultPermissions.remove(this);
    }

    public void setAllowedByDefaultOnTheWild(boolean allowed)
    {
        modified |= allowed != defaultWildValue;
        this.defaultWildValue = allowed;
        if(allowed)
            defaultWildPermissions.add(this);
        else
            defaultWildPermissions.remove(this);
    }

    public boolean isAllowedByDefaultOnTheWild()
    {
        return defaultWildValue;
    }

    /**
     * Sends the failure message to the player.
     * @param player The player that will receive the message
     * @param owner The owner of the protection location
     */
    public void notifyFailure(Player player, @Nullable PlayerName owner)
    {
        player.sendMessage(failureMessage(owner));
    }

    /**
     * Creates a formatted message that informs that a player is not allowed to do an action that is protected
     * by this permission.
     * @param owner The owner name to be added on the owner line, the line will be omitted if this value is {@code null}
     * @return The message with header and optionally with the owner line.
     */
    public Text failureMessage(@Nullable PlayerName owner)
    {
        Text.Builder message = header().color(TextColors.DARK_RED).append(failureMessage);
        if(owner != null)
            message.append(Text.NEW_LINE)
                    .append(Text.builder("Owner: ").color(TextColors.DARK_RED).build())
                    .append(Text.builder(owner.getName()).color(TextColors.GOLD).build());

        return message.build();
    }

    /**
     * Creates an uncompleted {@link Text.Builder} with the header part of the failure messages
     * @return The builder ready to be formatted
     */
    private static Text.Builder header()
    {
        return Text.builder("Permission> ");
    }

    @Override
    public boolean isModified()
    {
        return modified;
    }

    @Override
    public void setModified(boolean modified)
    {
        this.modified = modified;
    }

    public String getDescription()
    {
        return description;
    }
}
