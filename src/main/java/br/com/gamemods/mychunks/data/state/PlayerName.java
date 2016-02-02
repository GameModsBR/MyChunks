package br.com.gamemods.mychunks.data.state;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class PlayerName
{
    public static final PlayerName ADMINS = new PlayerName(UUID.nameUUIDFromBytes("MyChunks:Server-Admins".getBytes()), "Server Admins");

    private final UUID uniqueId;
    private String name;

    public PlayerName(UUID uniqueId, String name)
    {
        this.uniqueId = uniqueId;
        this.name = name;
    }

    public UUID getUniqueId()
    {
        return uniqueId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
