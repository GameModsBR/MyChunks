package br.com.gamemods.mychunks;

import com.flowpowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.EnumSet;

public class Util
{
    public static <E extends Enum<E>> EnumSet<E> enumSet(Class<E> enumClass, Collection<E> collection)
    {
        if(collection.isEmpty())
            return EnumSet.noneOf(enumClass);
        return EnumSet.copyOf(collection);
    }

    public static Vector3i blockToChunk(Vector3i position)
    {
        return new Vector3i(position.getX()>>4, 0, position.getZ()>>4);
    }
}
