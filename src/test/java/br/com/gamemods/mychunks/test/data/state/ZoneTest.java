package br.com.gamemods.mychunks.test.data.state;

import br.com.gamemods.mychunks.data.state.ClaimedChunk;
import br.com.gamemods.mychunks.data.state.WorldFallbackContext;
import br.com.gamemods.mychunks.data.state.Zone;
import com.flowpowered.math.vector.Vector3i;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;

import javax.naming.InvalidNameException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;

public class ZoneTest extends PermissionContextTest
{
    private WorldFallbackContext worldContext;
    private Zone zone1, zone2;

    @Before
    public void setUpContext() throws Exception
    {
        worldContext = new WorldFallbackContext(UUID.randomUUID());
        context = zone1 = new Zone(worldContext, "Test Zone A");
        zone2 = new Zone(worldContext, "Test Zone B");
    }

    @Test
    public void testName() throws Exception
    {
        try
        {
            zone1.setName(" invalid");
            throw new AssertionError("Accepted invalid name");
        }
        catch (InvalidNameException ignored)
        {}
        try
        {
            zone1.setName("invalid ");
            throw new AssertionError("Accepted invalid name");
        }
        catch (InvalidNameException ignored)
        {}

        try
        {
            zone1.setName("valid");
        }
        catch (InvalidNameException e)
        {
            throw new AssertionError("Rejected a valid name", e);
        }

        assertEquals("invalid1", zone1.setValidName(" invalid1"));
        assertEquals("invalid2", zone1.setValidName("invalid2 "));
        assertEquals("valid3", zone1.setValidName("valid3"));
        assertThat(zone1.setValidName("♥"), new BaseMatcher<String>()
        {
            @Override
            public void describeTo(Description description)
            {
                description.appendValue("Unnamed Zone [0-9]+");
            }

            @Override
            public boolean matches(Object item)
            {
                return item.toString().matches("Unnamed Zone [0-9]+");
            }
        });
    }

    @Test
    public void testAddRemoveChunk() throws Exception
    {
        Vector3i position = new Vector3i(5,0,9);
        UUID worldId = zone1.getWorldId();
        WorldFallbackContext worldContext = new WorldFallbackContext(worldId);
        ClaimedChunk claimedChunk = new ClaimedChunk(worldContext, position);
        ClaimedChunk diagonalChunk = new ClaimedChunk(worldContext, new Vector3i(6,0,10));
        ClaimedChunk crossChunk = new ClaimedChunk(worldContext, new Vector3i(4,0,9));
        ClaimedChunk crossChunk2 = new ClaimedChunk(worldContext, new Vector3i(3,0,9));
        ClaimedChunk farChunk = new ClaimedChunk(worldContext, new Vector3i(-10,0,4));

        ClaimedChunk dimensionalChunk = new ClaimedChunk(new WorldFallbackContext(UUID.randomUUID()), crossChunk.getPosition());

        // Add
        zone1.addChunk(claimedChunk);
        assertEquals(zone1, claimedChunk.getZone());
        Optional<ClaimedChunk> opt = zone1.getChunkAt(position);
        assertTrue(opt.isPresent());
        assertEquals(claimedChunk, opt.get());

        // Transfer
        zone2.addChunk(claimedChunk);
        assertEquals(zone2, claimedChunk.getZone());
        opt = zone1.getChunkAt(position);
        assertFalse(opt.isPresent());
        opt = zone2.getChunkAt(position);
        assertTrue(opt.isPresent());
        assertEquals(claimedChunk, opt.get());

        // Remove
        zone2.removeChunkAt(position);
        assertNull(claimedChunk.getZone());
        opt = zone2.getChunkAt(position);
        assertFalse(opt.isPresent());

        // Add diagonally
        zone1.addChunk(claimedChunk);
        try
        {
            zone1.addChunk(diagonalChunk);
            throw new AssertionError("Accepted a diagonal chunk");
        }
        catch (IllegalArgumentException ignored)
        {}

        // Add cross chunk
        zone1.addChunk(crossChunk);
        assertEquals(crossChunk, zone1.getChunkAt(crossChunk.getPosition()).get());

        // Add far chunk
        try
        {
            zone1.addChunk(farChunk);
            throw new AssertionError("Accepted a far chunk");
        }
        catch (IllegalArgumentException ignored)
        {}

        // Add a dimensional chunk
        try
        {
            zone1.addChunk(dimensionalChunk);
            throw new AssertionError("Accepted a dimensional chunk");
        }
        catch (IllegalArgumentException ignored)
        {}

        // Remove a chunk middle chunk
        zone1.addChunk(crossChunk2);
        try
        {
            zone1.removeChunkAt(crossChunk.getPosition());
            throw new AssertionError("Allowed to remove the middle chunk of a 3 chunk zone");
        }
        catch (IllegalArgumentException ignored)
        {}
    }
}