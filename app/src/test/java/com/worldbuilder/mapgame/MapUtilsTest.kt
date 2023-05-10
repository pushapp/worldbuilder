package com.worldbuilder.mapgame

import junit.framework.TestCase
import org.junit.Test

internal class MapUtilsTest {

    @Test
    fun testCreateIndices() {
        val result = MapUtils.createIndices(0, 10)
        TestCase.assertTrue(result.contains(0))
        TestCase.assertTrue(result.contains(1))
        TestCase.assertTrue(result.contains(2))
        TestCase.assertTrue(result.contains(9))
        TestCase.assertFalse(result.contains(10))
    }
}