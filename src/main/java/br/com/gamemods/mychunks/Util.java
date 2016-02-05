package br.com.gamemods.mychunks;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

@ParametersAreNonnullByDefault
@NonnullByDefault
public class Util
{
    public static final Collection<Vector3i> CARDINAL_DIRECTIONS = Collections.unmodifiableCollection(Arrays.asList(
            new Vector3i(-1,0,0),new Vector3i(1,0,0),
            new Vector3i(0,0,-1),new Vector3i(0,0,-1)
    ));

    public static <E extends Enum<E>> EnumSet<E> enumSet(Class<E> enumClass, @Nullable Collection<E> collection)
    {
        if(collection == null || collection.isEmpty())
            return EnumSet.noneOf(enumClass);
        return EnumSet.copyOf(collection);
    }

    @Deprecated
    public static Vector3i blockToChunk(Vector3i blockPosition)
    {
        return PositionType.BLOCK.toMinChunk(blockPosition);
    }

    /**
     * Obtain the region position that stores a chunk position
     * @return The X and Z coordinates
     * @see PositionType#toMinRegion(Vector3i)
     */
    public static Vector2i chunkToRegion(Vector3i chunkPosition)
    {
        return PositionType.discardHeight(PositionType.CHUNK.toMinRegion(chunkPosition));
    }

    /**
     * Remove anything that is not letters or digits from the string and convert what wasn't removed to lower case.
     * Accents are also removed from letters.
     * @param id The string to be worked
     * @return A string that matches {@code /^[\p{L}\p{Digit}]*$/}, note that it may be empty or have non-english letters and digits
     * like Chinese ideograms, Russian letters and Japanese numbers
     */
    public static String normalizeIdentifier(String id)
    {
        return Normalizer.normalize(id, Normalizer.Form.NFD)
                //.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^\\p{L}\\p{Digit}]", "")
                //.replaceAll("\\p{Blank}+", " ")
                .toLowerCase()
        ;
    }

    /**
     * Remove anything that is not English letters or digits from the string and convert what wasn't removed to lower case.
     * Accents are also removed from letters.
     * @param id The string to be worked
     * @return A string that matches {@code /^[a-zA-Z0-9]*$/}, note that it may be empty
     */
    public static String normalizeEnglishIdentifier(String id)
    {
        return Normalizer.normalize(id, Normalizer.Form.NFD)
                .replaceAll("[^a-zA-Z0-9]", "")
                //.replaceAll("\\p{Blank}+", " ")
                .toLowerCase()
        ;
    }
}
