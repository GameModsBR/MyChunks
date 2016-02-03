package br.com.gamemods.mychunks.test.data.state;

import br.com.gamemods.mychunks.data.state.*;
import com.flowpowered.math.vector.Vector3i;
import org.junit.Before;
import org.junit.Test;
import org.spongepowered.api.util.Tristate;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

public class ClaimedChunkTest extends PermissionContextTest
{
    ClaimedChunk chunk;
    @Before
    public void setUpContext() throws Exception
    {
        context = chunk = new ClaimedChunk(UUID.randomUUID(), new Vector3i(380, 0 , -568));
    }

    @Test
    public void testZonePermission() throws Exception
    {
        Zone zone = new Zone(UUID.randomUUID(), "Test Zone");
        zone.addChunk(chunk);

        assertFalse(chunk.check(Permission.MODIFY, owner));
        zone.setOwner(owner);
        assertTrue(chunk.check(Permission.MODIFY, owner));
        zone.setOwner(null);
        assertFalse(chunk.check(Permission.MODIFY, owner));

        assertFalse(chunk.check(Permission.MODIFY, builder));
        zone.addMember(new Member(builder, builderRank));
        assertTrue(chunk.check(Permission.MODIFY, builder));
        zone.removeChunkAt(chunk.getPosition());
        assertFalse(chunk.check(Permission.MODIFY, builder));

        zone.addChunk(chunk);
        assertTrue(chunk.check(Permission.MODIFY, builder));
        zone.removeMember(new Member(builder, builderRank));
        assertFalse(chunk.check(Permission.MODIFY, builder));
        zone.removeChunkAt(chunk.getPosition());
        assertFalse(chunk.check(Permission.MODIFY, builder));
    }
}