package br.com.gamemods.mychunks.test.data.state;

import br.com.gamemods.mychunks.data.state.*;
import com.flowpowered.math.vector.Vector3i;
import org.junit.Before;
import org.junit.Test;
import org.spongepowered.api.util.Tristate;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

public class ClaimedChunkTest
{
    ClaimedChunk chunk;
    @Before
    public void setUp() throws Exception
    {
        chunk = new ClaimedChunk(UUID.randomUUID(), new Vector3i(380, 0 , -568));
    }

    @Test
    public void testMembersAndPermissions() throws Exception
    {
        PlayerName owner = new PlayerName(UUID.randomUUID(), "Player Owner");
        assertFalse(chunk.check(Permission.MODIFY, owner.getUniqueId()));

        chunk.setOwner(owner);
        assertEquals(chunk.getOwner(), owner);
        assertTrue(chunk.check(Permission.MODIFY, owner.getUniqueId()));

        Rank builderRank = new Rank("builder", EnumSet.of(Permission.MODIFY));
        PlayerName builder = new PlayerName(UUID.randomUUID(), "Player Builder");
        assertFalse(chunk.check(Permission.MODIFY, builder.getUniqueId()));

        Member member = new Member(builder, builderRank);
        chunk.addMember(member);
        assertTrue(chunk.check(Permission.MODIFY, builder.getUniqueId()));
        assertTrue(chunk.removeMember(member));
        assertFalse(chunk.check(Permission.MODIFY, builder.getUniqueId()));

        chunk.setOwner(PlayerName.ADMINS);
        assertFalse(chunk.check(Permission.MODIFY, owner.getUniqueId()));

        assertTrue(chunk.setPublicPermission(Permission.MODIFY, Tristate.TRUE));
        assertTrue(chunk.check(Permission.MODIFY, owner.getUniqueId()));
        assertTrue(chunk.check(Permission.MODIFY, builder.getUniqueId()));

        assertTrue(chunk.setPublicPermission(Permission.ENTER, Tristate.FALSE));
        assertFalse(chunk.setPublicPermission(Permission.ENTER, Tristate.FALSE));
        assertFalse(chunk.check(Permission.ENTER, owner.getUniqueId()));
        assertFalse(chunk.check(Permission.ENTER, builder.getUniqueId()));

        assertTrue(chunk.setPublicPermission(Permission.ENTER, Tristate.UNDEFINED));
        assertFalse(chunk.setPublicPermission(Permission.ENTER, Tristate.UNDEFINED));
        assertTrue(chunk.check(Permission.MODIFY, owner.getUniqueId()));
        assertTrue(chunk.check(Permission.MODIFY, builder.getUniqueId()));

        assertTrue(chunk.setPublicPermission(Permission.MODIFY, Tristate.UNDEFINED));
        assertFalse(chunk.setPublicPermission(Permission.MODIFY, Tristate.UNDEFINED));
        assertFalse(chunk.check(Permission.MODIFY, owner.getUniqueId()));
        assertFalse(chunk.check(Permission.MODIFY, builder.getUniqueId()));
    }
}