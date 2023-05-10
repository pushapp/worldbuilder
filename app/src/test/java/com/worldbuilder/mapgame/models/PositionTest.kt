package com.worldbuilder.mapgame.models

import com.worldbuilder.mapgame.extensions.round
import junit.framework.TestCase
import org.junit.Test


internal class PositionTest {
    private val origin = Position(0, 0)
    private val one = Position(1, 1)
    private val two = Position(2, 2)

    @Test
    fun `is distance correct`() {
        val distance = origin.distance(one).round()
        TestCase.assertEquals(1.41, distance)
    }

    @Test
    fun `is sort by distance works`() {
        val somePos = Position(5, 5)
        val positions = listOf(one, somePos, two)
        val sorted = Position.sortByDistance(positions, origin)

        TestCase.assertEquals(positions.size, sorted.size)
        TestCase.assertEquals(sorted.first(), one)
        TestCase.assertEquals(sorted[1], two)
        TestCase.assertEquals(sorted[2], somePos)
    }
}