package com.worldbuilder.mapgame.models

import kotlin.math.sqrt

class Position(var x: Int = 0, var y: Int = 0) {

    operator fun set(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    fun distance(other: Position): Double {
        val dx = other.x - x
        val dy = other.y - y
        return sqrt((dx * dx + dy * dy).toDouble())
    }

    override fun toString(): String = "X = $x Y = $y"

    companion object {
        @JvmStatic
        fun sortByDistance(
            positions: List<Position>,
            referencePosition: Position
        ): List<Position> {
            return positions.sortedBy {
                it.distance(referencePosition)
            }
        }
    }
}