package br.com.gamemods.mychunks.data.state;

import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * A simple reference to a player. The name can be changed. It's recommended to reuse this instance to reflect
 * the name change immediately in all use cases.
 */
@ParametersAreNonnullByDefault
@NonnullByDefault
public class PlayerName
{
    /**
     * A fake player that refer to all server admins
     */
    public static final PlayerName ADMINS = new PlayerName(UUID.nameUUIDFromBytes("MyChunks:Server-Admins".getBytes()), "Server Admins");

    private final UUID uniqueId;
    private String name;

    /**
     * Construct a player reference using an unvalidated name.
     * @param uniqueId The ID used to identify this player, it may or may not be generated by Mojang
     * @param name The player name, can be invalid but can't be empty.
     * @throws IllegalArgumentException If the name is empty or contains only empty characters
     */
    public PlayerName(UUID uniqueId, String name) throws IllegalArgumentException
    {
        if(name.trim().isEmpty())
            throw new IllegalArgumentException("Empty name");

        this.uniqueId = uniqueId;
        this.name = name;
    }

    /**
     * The ID used to identify this player, it may or may not be generated by Mojang
     */
    public UUID getUniqueId()
    {
        return uniqueId;
    }

    /**
     * The player name. Be aware that it can be different from the current name used by the player and may not be a valid
     * minecraft player name.
     * @return A string that is not empty.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Update the player name. Note that it will update only this instance
     * @param name Does not need to be a valid Minecraft player name, does not need to be unique and does not need to be up to date with the
     *             current player name. The only restriction is that it can not be empty or contains only empty characters.
     * @throws IllegalArgumentException If the name is empty or contains only empty characters
     */
    public void setName(String name) throws IllegalArgumentException
    {
        this.name = name;
    }

    /**
     * Compares if two player instances refers to the same player
     * @param playerName The other player instance, can be {@code null}
     * @return {@code false} if the other instance is {@code null} or if it refers to an other player
     */
    public boolean equalsPlayer(@Nullable PlayerName playerName)
    {
        return playerName != null && playerName.getUniqueId().equals(uniqueId);
    }

    /**
     * Compares if two player instances refers to the same player based on the player UUID
     * @param playerUniqueId The other player UUID, can be {@code null}
     * @return {@code false} if the passed UUID is {@code null} or if it refers to an other player
     */
    public boolean equalsPlayer(@Nullable UUID playerUniqueId)
    {
        return playerUniqueId != null && playerUniqueId.equals(uniqueId);
    }
}