package br.com.gamemods.mychunks.test.data.state;

import br.com.gamemods.mychunks.data.state.*;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.spongepowered.api.util.Tristate;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
    public void testAOwner() throws Exception
    {
        assertFalse("The test subject does not have modify permission by default",
                context.check(Permission.MODIFY, owner.getUniqueId())
        );

        context.setOwner(owner);
        assertEquals("The test subject was correctly defined as owner of the context",
                context.getOwner().orElse(null), owner
        );
        assertTrue("The owner of the context has modify permission",
                context.check(Permission.MODIFY, owner.getUniqueId())
        );

        assertFalse("An outsider does not have modify permission",
                context.check(Permission.MODIFY, builder.getUniqueId())
        );

        context.setOwner(null);
        assertFalse("An ex-owner does not have modify permission anymore",
                context.check(Permission.MODIFY, owner.getUniqueId())
        );
    }

    @Test
    public void testAMember() throws Exception
    {
        Member member = new Member(builder, builderRank);
        context.addMember(member);
        assertTrue("A rank permission is granted",
                context.check(Permission.MODIFY, builder.getUniqueId())
        );
        assertTrue("The rank association is correctly removed",
                context.removeMember(member)
        );
        assertFalse("The ex-builder does not have modify permission anymore",
                context.check(Permission.MODIFY, builder.getUniqueId())
        );

    }

    @Test
    public void testBPublicPermission() throws Exception
    {
        assertTrue("The public context didn't have modify permission set to true",
                context.setPublicPermission(Permission.MODIFY, Tristate.TRUE)
        );


        assertTrue("The ex-owner now have modify permission",
                context.check(Permission.MODIFY, owner.getUniqueId())
        );
        assertTrue("The ex-builder now have modify permission",
                context.check(Permission.MODIFY, builder.getUniqueId())
        );

        assertTrue("The public context didn't had enter permission set to false",
                context.setPublicPermission(Permission.ENTER, Tristate.FALSE)
        );
        assertFalse("The setPublicPermission doesn't return true when nothing is changed",
                context.setPublicPermission(Permission.ENTER, Tristate.FALSE)
        );
        assertFalse("The ex-owner does not have enter permission when the public permission is defined to false on the context",
                context.check(Permission.ENTER, owner.getUniqueId())
        );
        assertFalse("The ex-builder does not have enter permission when the public permission is defined to false on the context",
                context.check(Permission.ENTER, builder.getUniqueId())
        );

        assertTrue("The enter permission was correctly unset",
                context.setPublicPermission(Permission.ENTER, Tristate.UNDEFINED)
        );
        assertFalse("The enter permission was correctly unset (second check)",
                context.setPublicPermission(Permission.ENTER, Tristate.UNDEFINED)
        );

        assertTrue("The ex-owner still have modify permission",
                context.check(Permission.MODIFY, owner.getUniqueId())
        );
        assertTrue("The ex-builder still have modify permission",
                context.check(Permission.MODIFY, builder.getUniqueId())
        );

        assertTrue("The public modify permission was correctly unset",
                context.setPublicPermission(Permission.MODIFY, Tristate.UNDEFINED)
        );
        assertFalse("The public modify permission was correctly unset (second check)",
                context.setPublicPermission(Permission.MODIFY, Tristate.UNDEFINED)
        );

        assertFalse("The ex-owner does not have modify permission anymore",
                context.check(Permission.MODIFY, owner.getUniqueId())
        );
        assertFalse("The ex-builder does not have modify permission anymore",
                context.check(Permission.MODIFY, builder.getUniqueId())
        );
    }
}
