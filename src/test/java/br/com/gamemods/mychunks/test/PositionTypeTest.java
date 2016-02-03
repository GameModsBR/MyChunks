package br.com.gamemods.mychunks.test;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import org.junit.Test;

import static org.junit.Assert.*;
import static br.com.gamemods.mychunks.PositionType.*;

public class PositionTypeTest
{
    @Test
    public void testTypes()
    {
        Vector3i block = new Vector3i(5988, 55, 6588);
        Vector3i minChunkSectionBlock = new Vector3i(5984, 48, 6576);
        Vector3i minChunkBlock = new Vector3i(5984, 0, 6576);
        Vector3i chunkSection = new Vector3i(374, 3, 411);
        Vector3i chunk = new Vector3i(374, 0, 411);
        Vector3i region = new Vector3i(11, 0, 12);

        // Conversions by direct invocations

        assertEquals(chunkSection, BLOCK.toMaxChunkSection(block));
        assertEquals(chunk, BLOCK.toMaxChunk(block));
        assertEquals(region, BLOCK.toMaxRegion(block));

        assertEquals(minChunkSectionBlock, CHUNK_SECTION.toMinBlock(chunkSection));
        assertEquals(chunk, CHUNK_SECTION.toMinChunk(chunkSection));
        assertEquals(region, CHUNK_SECTION.toMinRegion(chunkSection));

        assertEquals(minChunkBlock, CHUNK.toMinBlock(chunk));
        assertEquals(chunk, CHUNK.toMinChunkSection(chunk));
        assertEquals(region, CHUNK.toMinRegion(chunk));


        Vector3i minRegionBlock = new Vector3i(5632,0, 6144);
        Vector3i minRegionChunk = new Vector3i(352,0,384);
        assertEquals(minRegionBlock, REGION.toMinBlock(region));
        assertEquals(minRegionChunk, REGION.toMinChunkSection(region));
        assertEquals(minRegionChunk, REGION.toMinChunk(region));

        // Conversion using toMin

        assertEquals(chunkSection, BLOCK.toMin(CHUNK_SECTION, block));
        assertEquals(chunk, BLOCK.toMin(CHUNK, block));
        assertEquals(region, BLOCK.toMin(REGION, block));

        assertEquals(minChunkSectionBlock, CHUNK_SECTION.toMin(BLOCK, chunkSection));
        assertEquals(chunk, CHUNK_SECTION.toMin(CHUNK, chunkSection));
        assertEquals(region, CHUNK_SECTION.toMin(REGION, chunkSection));

        assertEquals(minChunkBlock, CHUNK.toMin(BLOCK, chunk));
        assertEquals(chunk, CHUNK.toMin(CHUNK_SECTION, chunk));
        assertEquals(region, CHUNK.toMin(REGION, chunk));

        assertEquals(minRegionBlock, REGION.toMin(BLOCK, region));
        assertEquals(minRegionChunk, REGION.toMin(CHUNK_SECTION, region));
        assertEquals(minRegionChunk, REGION.toMin(CHUNK, region));

        // Maxes

        assertEquals(chunkSection, CHUNK_SECTION.maxFrom(BLOCK, block));
        assertEquals(chunk, CHUNK.maxFrom(BLOCK, block));
        assertEquals(region, REGION.maxFrom(BLOCK, block));

        Vector3i maxChunkSectionBlock = new Vector3i(5999,63,6591);
        assertEquals(maxChunkSectionBlock, BLOCK.maxFrom(CHUNK_SECTION, chunkSection));
        assertEquals(chunk, CHUNK.maxFrom(CHUNK_SECTION, chunkSection));
        assertEquals(region, REGION.maxFrom(CHUNK_SECTION, chunkSection));

        Vector3i maxChunkBlock = new Vector3i(5999,255,6591);
        Vector3i maxChunkChunkSection = new Vector3i(374, 15, 411);
        assertEquals(maxChunkBlock, BLOCK.maxFrom(CHUNK, chunk));
        assertEquals(maxChunkChunkSection, CHUNK_SECTION.maxFrom(CHUNK, chunk));
        assertEquals(region, REGION.maxFrom(CHUNK, chunk));

        Vector3i maxRegionBlock = new Vector3i(6143,255,6655);
        Vector3i maxRegionChunkSection = new Vector3i(383,15,415);
        Vector3i maxRegionChunk = new Vector3i(383,0,415);
        assertEquals(maxRegionBlock, BLOCK.maxFrom(REGION, region));
        assertEquals(maxRegionChunkSection, CHUNK_SECTION.maxFrom(REGION, region));
        assertEquals(maxRegionChunk, CHUNK.maxFrom(REGION, region));

        //Vector3i minChunkSectionBlock = new Vector3i(5984, 48, 6576);
        Vector2i noHeight = discardHeight(minChunkSectionBlock);
        assertEquals(new Vector2i(5984, 6576), noHeight);
        assertEquals(minChunkSectionBlock, addHeight(noHeight, 48));
    }
}