package br.com.gamemods.mychunks;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * The type of a position reference. This enum allows easy conversions from and to all types.
 */
@ParametersAreNonnullByDefault
@NonnullByDefault
public enum PositionType
{
    /**
     * A block position in a world
     */
    BLOCK{
        @Override
        public Vector3i minFrom(PositionType fromType, Vector3i fromPosition)
        {
            return fromType.toMinBlock(fromPosition);
        }

        @Override
        public Vector3i maxFrom(PositionType fromType, Vector3i fromPosition)
        {
            return fromType.toMaxBlock(fromPosition);
        }

        @Override
        public Vector3i toMinBlock(Vector3i position)
        {
            return position;
        }

        @Override
        public Vector3i toMaxBlock(Vector3i position)
        {
            return position;
        }

        @Override
        public Vector3i toMinChunkSection(Vector3i position)
        {
            return new Vector3i(position.getX()>>4,position.getY()>>4,position.getZ()>>4);
        }

        @Override
        public Vector3i toMaxChunkSection(Vector3i position)
        {
            return toMinChunkSection(position);
        }

        @Override
        public Vector3i toMinChunk(Vector3i position)
        {
            return new Vector3i(position.getX()>>4, 0, position.getZ()>>4);
        }

        @Override
        public Vector3i toMaxChunk(Vector3i position)
        {
            return toMinChunk(position);
        }

        @Override
        public Vector3i toMinRegion(Vector3i position)
        {
            return CHUNK.toMinRegion(toMinChunk(position));
        }

        @Override
        public Vector3i toMaxRegion(Vector3i position)
        {
            return toMinRegion(position);
        }
    },

    /**
     * A chunk with height divided in sections of 16 blocks
     */
    CHUNK_SECTION{
        @Override
        public Vector3i minFrom(PositionType fromType, Vector3i fromPosition)
        {
            return fromType.toMinChunkSection(fromPosition);
        }

        @Override
        public Vector3i maxFrom(PositionType fromType, Vector3i fromPosition)
        {
            return fromType.toMaxChunkSection(fromPosition);
        }

        @Override
        public Vector3i toMinBlock(Vector3i position)
        {
            return new Vector3i(position.getX()<<4, position.getY()<<4,position.getZ()<<4);
        }

        @Override
        public Vector3i toMaxBlock(Vector3i position)
        {
            return toMinBlock(position).add(15, 15, 15);
        }

        @Override
        public Vector3i toMinChunkSection(Vector3i position)
        {
            return position;
        }

        @Override
        public Vector3i toMaxChunkSection(Vector3i position)
        {
            return position;
        }

        @Override
        public Vector3i toMinChunk(Vector3i position)
        {
            return position.getY() == 0? position : new Vector3i(position.getX(), 0, position.getZ());
        }

        @Override
        public Vector3i toMaxChunk(Vector3i position)
        {
            return toMinChunk(position);
        }

        @Override
        public Vector3i toMinRegion(Vector3i position)
        {
            return new Vector3i(position.getX()>>5, 0, position.getZ()>>5);
        }

        @Override
        public Vector3i toMaxRegion(Vector3i position)
        {
            return toMinRegion(position);
        }
    },

    /**
     * An entire chunk that groups 16 blocks from X and Z coordinates and 256 blocks
     */
    CHUNK{
        @Override
        public Vector3i minFrom(PositionType fromType, Vector3i fromPosition)
        {
            return fromType.toMinChunk(fromPosition);
        }

        @Override
        public Vector3i maxFrom(PositionType fromType, Vector3i fromPosition)
        {
            return fromType.toMaxChunk(fromPosition);
        }

        @Override
        public Vector3i toMinBlock(Vector3i position)
        {
            return new Vector3i(position.getX()<<4, 0, position.getZ()<<4);
        }

        @Override
        public Vector3i toMaxBlock(Vector3i position)
        {
            return toMinBlock(position).add(15,255,15);
        }

        @Override
        public Vector3i toMinChunkSection(Vector3i position)
        {
            return position.getY() == 0? position : new Vector3i(position.getX(), 0, position.getZ());
        }

        @Override
        public Vector3i toMaxChunkSection(Vector3i position)
        {
            return position.getY() == 15? position : new Vector3i(position.getX(), 15, position.getZ());
        }

        @Override
        public Vector3i toMinChunk(Vector3i position)
        {
            return position;
        }

        @Override
        public Vector3i toMaxChunk(Vector3i position)
        {
            return position;
        }

        @Override
        public Vector3i toMinRegion(Vector3i position)
        {
            return new Vector3i(position.getX()>>5, 0, position.getZ()>>5);
        }

        @Override
        public Vector3i toMaxRegion(Vector3i position)
        {
            return toMinRegion(position);
        }
    },

    /**
     * A group of chunks that stores 32 chunks on X and Z coordinates.
     */
    REGION{
        @Override
        public Vector3i minFrom(PositionType fromType, Vector3i fromPosition)
        {
            return fromType.toMinRegion(fromPosition);
        }

        @Override
        public Vector3i maxFrom(PositionType fromType, Vector3i fromPosition)
        {
            return fromType.toMaxRegion(fromPosition);
        }

        @Override
        public Vector3i toMinBlock(Vector3i position)
        {
            return CHUNK.toMinBlock(toMinChunk(position));
        }

        @Override
        public Vector3i toMaxBlock(Vector3i position)
        {
            return CHUNK.toMaxBlock(toMaxChunk(position));
        }

        @Override
        public Vector3i toMinChunkSection(Vector3i position)
        {
            return new Vector3i(position.getX()<<5, position.getY()<<5, position.getZ()<<5);
        }

        @Override
        public Vector3i toMaxChunkSection(Vector3i position)
        {
            return toMinChunkSection(position).add(31,15,31);
        }

        @Override
        public Vector3i toMinChunk(Vector3i position)
        {
            return new Vector3i(position.getX()<<5, position.getY()<<5, position.getZ()<<5);
        }

        @Override
        public Vector3i toMaxChunk(Vector3i position)
        {
            return toMinChunk(position).add(31, 0, 31);
        }

        @Override
        public Vector3i toMinRegion(Vector3i position)
        {
            return position;
        }

        @Override
        public Vector3i toMaxRegion(Vector3i position)
        {
            return position;
        }
    }
    ;

    /**
     * Calculate the minimum position that can be contained in a specific coordinate
     * @param fromType The original coordinate type
     * @param fromPosition The coordinate to be checked
     * @return The minimum coordinate or the coordinate that this type is stored. The coordinate unit is based on this type.
     */
    public abstract Vector3i minFrom(PositionType fromType, Vector3i fromPosition);

    /**
     * Calculate the maximum position that can be contained in a specific coordinate
     * @param fromType The original coordinate type
     * @param fromPosition The coordinate to be checked
     * @return The maximum coordinate or the coordinate that this type is stored. The coordinate unit is based on this type.
     */
    public abstract Vector3i maxFrom(PositionType fromType, Vector3i fromPosition);

    /**
     * Calculate the minimum position that can be contained in a specific coordinate
     * @param toType The resulting coordinate type
     * @param fromPosition The coordinate to be checked based on this type
     * @return The minimum coordinate or the coordinate that this type is stored
     */
    public Vector3i toMin(PositionType toType, Vector3i fromPosition)
    {
        return toType.minFrom(this, fromPosition);
    }

    /**
     * Calculate the maximum position that can be contained in a specific coordinate
     * @param toType The resulting coordinate type
     * @param fromPosition The coordinate to be checked based on this type
     * @return The maximum coordinate or the coordinate that this type is stored
     */
    public Vector3i toMax(PositionType toType, Vector3i fromPosition)
    {
        return toType.maxFrom(this, fromPosition);
    }

    /**
     * Calculate the minimum block position that can be contained in a specific coordinate
     * @param position The coordinate to be checked based on this type
     * @return The minimum block coordinate or the coordinate that this type is stored
     */
    public abstract Vector3i toMinBlock(Vector3i position);

    /**
     * Calculate the maximum block position that can be contained on a specific coordinate
     * @param position The coordinate to be checked based on this type
     * @return The maximum block coordinate or the coordinate that this type is stored
     */
    public abstract Vector3i toMaxBlock(Vector3i position);

    /**
     * Calculate the minimum chunk section that can be contained on a specif coordinate
     * @param position The coordinate to be checked based on this type
     * @return The minimum chunk section coordinate or the coordinate that this type is stored
     */
    public abstract Vector3i toMinChunkSection(Vector3i position);

    /**
     * Calculate the maximum chunk section that can be contained on a specif coordinate
     * @param position The coordinate to be checked based on this type
     * @return The maximum chunk section coordinate or the coordinate that this type is stored
     */
    public abstract Vector3i toMaxChunkSection(Vector3i position);

    /**
     * Calculate the minimum chunk that can be contained on a specif coordinate
     * @param position The coordinate to be checked based on this type
     * @return The minimum chunk coordinate or the coordinate that this type is stored
     */
    public abstract Vector3i toMinChunk(Vector3i position);

    /**
     * Calculate the maximum chunk that can be contained on a specif coordinate
     * @param position The coordinate to be checked based on this type
     * @return The maximum chunk coordinate or the coordinate that this type is stored
     */
    public abstract Vector3i toMaxChunk(Vector3i position);

    /**
     * Calculate the minimum region that can be contained on a specif coordinate
     * @param position The coordinate to be checked based on this type
     * @return The minimum region coordinate or the coordinate that this type is stored
     */
    public abstract Vector3i toMinRegion(Vector3i position);

    /**
     * Calculate the maximum region that can be contained on a specif coordinate
     * @param position The coordinate to be checked based on this type
     * @return The maximum region coordinate or the coordinate that this type is stored
     */
    public abstract Vector3i toMaxRegion(Vector3i position);

    /**
     * Creates a 2 coordinate vector that represents X and Z coordinates in a world.
     * @param position The XYZ coordinates
     * @return The XZ coordinates, the Y property on the vector represents the Z coordinate.
     */
    public static Vector2i discardHeight(Vector3i position)
    {
        return new Vector2i(position.getX(), position.getZ());
    }

    /**
     * Adds the height to a position
     * @param position The 2 coordinates position that represents the world's X and Z instead of X and Y.
     * @param height The height to be added
     * @return A vector that correctly represents the world's X Y and Z coordinates
     */
    public static Vector3i addHeight(Vector2i position, int height)
    {
        return new Vector3i(position.getX(), height, position.getY());
    }
}
