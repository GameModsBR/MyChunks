package br.com.gamemods.mychunks.test.data.state;

import br.com.gamemods.mychunks.data.state.*;
import org.junit.Before;
import org.junit.Test;
import org.spongepowered.api.util.Tristate;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

public class PermissionContextTest
{
    protected OwnedContext context;
    protected PlayerName owner, builder;
    protected Rank builderRank;

    @Before
    public void setUpContext() throws Exception
    {
        context = new OwnedContext();
    }

    @Before
    public void setUpUsers() throws Exception
    {
        owner = new PlayerName(UUID.randomUUID(), "Player Owner");
        builderRank = new Rank("builder", EnumSet.of(Permission.MODIFY));
        builder = new PlayerName(UUID.randomUUID(), "Player Builder");
    }

    @Test
    public void testMembersAndPermissions() throws Exception
    {
        assertFalse(context.check(Permission.MODIFY, owner.getUniqueId()));

        context.setOwner(owner);
        assertTrue(context.getOwner().isPresent());
        assertEquals(context.getOwner().get(), owner);
        assertTrue(context.check(Permission.MODIFY, owner.getUniqueId()));

        assertFalse(context.check(Permission.MODIFY, builder.getUniqueId()));

        Member member = new Member(builder, builderRank);
        context.addMember(member);
        assertTrue(context.check(Permission.MODIFY, builder.getUniqueId()));
        assertTrue(context.removeMember(member));
        assertFalse(context.check(Permission.MODIFY, builder.getUniqueId()));

        context.setOwner(null);
        assertFalse(context.check(Permission.MODIFY, owner.getUniqueId()));

        assertTrue(context.setPublicPermission(Permission.MODIFY, Tristate.TRUE));
        assertTrue(context.check(Permission.MODIFY, owner.getUniqueId()));
        assertTrue(context.check(Permission.MODIFY, builder.getUniqueId()));

        assertTrue(context.setPublicPermission(Permission.ENTER, Tristate.FALSE));
        assertFalse(context.setPublicPermission(Permission.ENTER, Tristate.FALSE));
        assertFalse(context.check(Permission.ENTER, owner.getUniqueId()));
        assertFalse(context.check(Permission.ENTER, builder.getUniqueId()));

        assertTrue(context.setPublicPermission(Permission.ENTER, Tristate.UNDEFINED));
        assertFalse(context.setPublicPermission(Permission.ENTER, Tristate.UNDEFINED));
        assertTrue(context.check(Permission.MODIFY, owner.getUniqueId()));
        assertTrue(context.check(Permission.MODIFY, builder.getUniqueId()));

        assertTrue(context.setPublicPermission(Permission.MODIFY, Tristate.UNDEFINED));
        assertFalse(context.setPublicPermission(Permission.MODIFY, Tristate.UNDEFINED));
        assertFalse(context.check(Permission.MODIFY, owner.getUniqueId()));
        assertFalse(context.check(Permission.MODIFY, builder.getUniqueId()));
    }
}
