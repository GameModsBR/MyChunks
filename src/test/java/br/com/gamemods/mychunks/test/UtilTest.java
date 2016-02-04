package br.com.gamemods.mychunks.test;

import br.com.gamemods.mychunks.data.state.Permission;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;

import static br.com.gamemods.mychunks.Util.*;
import static org.junit.Assert.assertEquals;

public class UtilTest
{
    @Test
    public void testEnumSetDefault() throws Exception
    {
        EnumSet<Permission> permissions = EnumSet.allOf(Permission.class);
        permissions.removeIf(p->!p.isAllowedByDefault());
        assertEquals(permissions, Permission.getDefaultPermissions());

        permissions = EnumSet.allOf(Permission.class);
        permissions.removeIf(p->!p.isAllowedByDefaultOnTheWild());
        assertEquals(permissions, Permission.getDefaultWildPermissions());
    }

    @Test
    public void testEnumSetSingle() throws Exception
    {
        assertEquals(EnumSet.of(Permission.MODIFY), enumSet(Permission.class, Collections.singletonList(Permission.MODIFY)));
    }

    @Test
    public void testEnumSetNull() throws Exception
    {
        assertEquals(Collections.emptySet(), enumSet(Permission.class, null));
    }

    @Test
    public void testEnumSetEmpty() throws Exception
    {
        assertEquals(EnumSet.noneOf(Permission.class), enumSet(Permission.class, new ArrayList<>(0)));
    }

    @Test
    public void testNormalizeIdentifier() throws Exception
    {
        assertEquals("apple", normalizeIdentifier("♥ã♦♣ṕ♠Ṗ•◘ĺ○◙♂♀ê♪♫☼►◄"));
        assertEquals("символы", normalizeIdentifier("сим♣волы"));
        assertEquals("tahemarki", normalizeIdentifier("täHemärki"));
        assertEquals("文e字", normalizeIdentifier("文é字"));
        assertEquals("字符c", normalizeIdentifier("字符ç"));
        assertEquals("5568aceим4a", normalizeIdentifier("5¢5^68ãçêим4 - a"));
        assertEquals("2", normalizeIdentifier("１2➌➃❺"));
    }

    @Test
    public void testNormalizeEnglishIdentifier() throws Exception
    {
        assertEquals("apple", normalizeEnglishIdentifier("♥ã♦♣ṕ♠Ṗ•◘ĺ○◙♂♀ê♪♫☼►◄"));
        assertEquals("e", normalizeEnglishIdentifier("сим♣воèлы"));
        assertEquals("tahemarki", normalizeEnglishIdentifier("täHemärki"));
        assertEquals("e", normalizeEnglishIdentifier("文é字"));
        assertEquals("c", normalizeEnglishIdentifier("字符ç"));
        assertEquals("5568ace4a", normalizeEnglishIdentifier("5¢5^68ãçê4 - a"));
        assertEquals("2", normalizeIdentifier("１2➌➃❺"));
    }
}