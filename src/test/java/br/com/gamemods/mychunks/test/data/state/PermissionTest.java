package br.com.gamemods.mychunks.test.data.state;

import br.com.gamemods.mychunks.data.state.Permission;
import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.*;
import static br.com.gamemods.mychunks.data.state.Permission.*;

public class PermissionTest
{
    @Test
    public void testDefault() throws Exception
    {
        EnumSet<Permission> baseDefault = getDefaultPermissions();
        EnumSet<Permission> modified = EnumSet.copyOf(baseDefault);

        MODIFY.setAllowedByDefault(true);
        assertTrue(MODIFY.isAllowedByDefault());
        modified.add(MODIFY);
        assertEquals(modified, getDefaultPermissions());

        MODIFY.setAllowedByDefault(false);
        assertEquals(baseDefault, getDefaultPermissions());
    }
}