package br.com.gamemods.mychunks.data.state;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;

@ParametersAreNonnullByDefault
public class Rank
{
    private String name;
    private EnumSet<Permission> permissions;

    public Rank(String name, EnumSet<Permission> permissions)
    {
        this.name = name;
        this.permissions = permissions;
    }

    public Rank(String name)
    {
        this.name = name;
        this.permissions = EnumSet.noneOf(Permission.class);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public EnumSet<Permission> getPermissions()
    {
        return permissions;
    }

    public void setPermissions(EnumSet<Permission> permissions)
    {
        this.permissions = permissions;
    }
}
