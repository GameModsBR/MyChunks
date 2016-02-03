package br.com.gamemods.mychunks.test.data.state;

import br.com.gamemods.mychunks.data.state.*;
import com.flowpowered.math.vector.Vector3i;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.spongepowered.api.util.Tristate;

import javax.naming.InvalidNameException;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;

public class ZoneTest extends PermissionContextTest
{
    private Zone zone1, zone2;

    @Before
    public void setUpContext() throws Exception
    {
        context = zone1 = new Zone(UUID.randomUUID(), "Test Zone A");
        zone2 = new Zone(UUID.randomUUID(), "Test Zone B");
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
        ClaimedChunk claimedChunk = new ClaimedChunk(zone1.getWorldId(), position);

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
    }
}