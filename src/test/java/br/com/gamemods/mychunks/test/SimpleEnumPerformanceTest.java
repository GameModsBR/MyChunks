package br.com.gamemods.mychunks.test;

import br.com.gamemods.mychunks.data.state.Permission;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.EnumSet;

@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SimpleEnumPerformanceTest
{
    private static final int REPETITIONS = 500000000;

    @Test
    public void testAClassLoading() throws Exception
    {
        Permission.values();
    }

    @Test
    public void testBEnumPerformanceWithValues() throws Exception
    {
        long time = System.currentTimeMillis();
        int i = 0;
        for(; i < REPETITIONS; i++)
            Permission.values();
        time = System.currentTimeMillis() - time;
        System.out.println("Time spent on "+i+" values() calls:\t"+time);
    }

    @Test
    public void testCEnumPerformanceWithEnumSet() throws Exception
    {
        long time = System.currentTimeMillis();
        int i = 0;
        for(; i < REPETITIONS; i++)
            EnumSet.allOf(Permission.class);
        time = System.currentTimeMillis() - time;
        System.out.println("Time spent on "+i+" EnumSet.allOf() calls:\t"+time);
    }

    @Test
    public void testDEnumPerformanceWithValuesIterations() throws Exception
    {
        long time = System.currentTimeMillis();
        int i = 0;
        for(; i < REPETITIONS; i++)
            for(Permission ignored: Permission.values());

        time = System.currentTimeMillis() - time;
        System.out.println("Time spent on "+i+" values() full iterations:\t"+time);
    }

    @Test
    public void testEEnumPerformanceWithEnumSetIterations() throws Exception
    {
        long time = System.currentTimeMillis();
        int i = 0;
        for(; i < REPETITIONS; i++)
            for(Permission ignored: EnumSet.allOf(Permission.class));
        time = System.currentTimeMillis() - time;
        System.out.println("Time spent on "+i+" EnumSet.allOf() full iterations:\t"+time);
    }
}
