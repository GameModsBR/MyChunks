package br.com.gamemods.mychunks.test.data.state;

import br.com.gamemods.mychunks.data.state.Permission;
import org.junit.Test;

import java.util.EnumSet;

import static br.com.gamemods.mychunks.data.state.Permission.*;
import static org.junit.Assert.*;

public class PermissionTest
{
    @Test
    public void testDefault() throws Exception
    {
        assertFalse(getDefaultPermissions().isEmpty());
        assertFalse(getDefaultPermissions().containsAll(EnumSet.allOf(Permission.class)));
        assertFalse(getDefaultWildPermissions().isEmpty());

        EnumSet<Permission> baseDefault = getDefaultPermissions();
        EnumSet<Permission> modified = EnumSet.copyOf(baseDefault);

        MODIFY.setAllowedByDefault(true);
        assertTrue(MODIFY.isAllowedByDefault());
        modified.add(MODIFY);
        assertEquals(modified, getDefaultPermissions());

        MODIFY.setAllowedByDefault(false);
        assertEquals(baseDefault, getDefaultPermissions());

        baseDefault = getDefaultWildPermissions();
        modified = EnumSet.copyOf(baseDefault);

        MODIFY.setAllowedByDefaultOnTheWild(false);
        assertFalse(MODIFY.isAllowedByDefault());
        modified.remove(MODIFY);
        assertEquals(modified, getDefaultWildPermissions());

        MODIFY.setAllowedByDefaultOnTheWild(true);
        assertTrue(MODIFY.isAllowedByDefaultOnTheWild());
        modified.add(MODIFY);
        assertEquals(modified, getDefaultWildPermissions());
    }
}