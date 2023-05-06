package com.worldbuilder.mapgame;

import junit.framework.TestCase;

import java.util.ArrayList;

public class MapUtilsTest extends TestCase {

    public void testCreateIndices() {
        ArrayList<Integer> result = MapUtils.createIndices(0, 10);
        assertTrue(result.contains(0));
        assertTrue(result.contains(1));
        assertTrue(result.contains(2));
        assertTrue(result.contains(9));
        assertFalse(result.contains(10));
    }
}