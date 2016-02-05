package br.com.gamemods.mychunks.test.data.state;

import br.com.gamemods.mychunks.data.state.*;
import com.flowpowered.math.vector.Vector3i;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClaimedChunkTest extends PermissionContextTest
{
    private ClaimedChunk integratedChunk;
    private ClaimedChunk soldChunk;
    private Zone zone;
    private PlayerName zoneOwner;
    private Member zoneBuilder;

    @Before
    public void setUpContext() throws Exception
    {
        zoneOwner = new PlayerName(UUID.randomUUID(), "Zone Owner");
        zoneBuilder = new Member(new PlayerName(UUID.randomUUID(), "Zone Builder"), builderRank);

        WorldFallbackContext world = new WorldFallbackContext(UUID.randomUUID());

        context = integratedChunk = new ClaimedChunk(world, new Vector3i(380, 0, -568));
        integratedChunk.setOwner(zoneOwner);

        soldChunk = new ClaimedChunk(world, new Vector3i(381, 0, -568));
        soldChunk.setOwner(new PlayerName(UUID.randomUUID(), "Buyer"));

        zone = new Zone(integratedChunk.getWorldContext(), "Test Zone");
        zone.setOwner(zoneOwner);
        zone.addChunk(integratedChunk);
        zone.addChunk(soldChunk);
    }

    @Test
    public void testAZoneOwner() throws Exception
    {
        assertTrue("The owner of the zone have modify permission on a fully integrated chunk",
                integratedChunk.check(Permission.MODIFY, zoneOwner)
        );
        assertFalse("The owner of the zone does not have modify permission on a sold chunk",
                soldChunk.check(Permission.MODIFY, zoneOwner)
        );

        zone.setOwner(null);
        assertFalse("The ex-zone owner does not have modify permission on a fully integrated chunk anymore",
                integratedChunk.check(Permission.MODIFY, zoneOwner)
        );
        assertFalse("The ex-zone owner still does not have modify permission on a sold chunk",
                soldChunk.check(Permission.MODIFY, zoneOwner)
        );

        zone.setOwner(zoneOwner);
        assertTrue("The owner of the zone regained modify permission on a fully integrated chunk",
                integratedChunk.check(Permission.MODIFY, zoneOwner)
        );
        assertFalse("The owner still does not have modify permission on a sold chunk after restoring the zone owner position",
                soldChunk.check(Permission.MODIFY, zoneOwner)
        );
    }

    @Test
    public void testBZoneMember() throws Exception
    {
        PlayerName builder = zoneBuilder.getPlayerId();
        assertFalse("The test subject does not have modify permission on the integrated chunk",
                integratedChunk.check(Permission.MODIFY, builder)
        );
        assertFalse("The test subject does not have modify permission on the sold chunk",
                soldChunk.check(Permission.MODIFY, builder)
        );

        zone.addMember(zoneBuilder);
        assertTrue("A zone builder has modify permission on a fully integrated chunk",
                integratedChunk.check(Permission.MODIFY, builder)
        );
        assertFalse("A zone builder does not have modify permission on a sold chunk",
                soldChunk.check(Permission.MODIFY, builder)
        );

        zone.removeChunkAt(integratedChunk.getPosition());
        assertFalse("A zone builder does not have permission on a chunk that was removed from the zone",
                integratedChunk.check(Permission.MODIFY, builder)
        );

        zone.addChunk(integratedChunk);
        assertTrue("A zone builder regains modify permission on a fully integrated chunk that was re-added to the zone",
                integratedChunk.check(Permission.MODIFY, builder)
        );

        // New member instance created intentionally for testing
        zone.removeMember(new Member(builder, builderRank));
        assertFalse("An ex-builder of the zone does not have modify permission on a fully integrated chunk",
                integratedChunk.check(Permission.MODIFY, builder)
        );

        zone.addMember(zoneBuilder);
        assertTrue("A zone builder regained modify permission on a fully integrated chunk",
                integratedChunk.check(Permission.MODIFY, builder)
        );
        assertFalse("A zone builder did not gain modify permission on a sold chunk",
                soldChunk.check(Permission.MODIFY, builder)
        );

        zone.removeChunkAt(integratedChunk.getPosition());
        assertFalse("An ex-builder of the zone does not have modify permission on a integrated chunk",
                integratedChunk.check(Permission.MODIFY, builder)
        );
    }
}