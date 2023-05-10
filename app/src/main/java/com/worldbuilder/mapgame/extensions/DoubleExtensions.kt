package com.worldbuilder.mapgame.extensions

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * rounds double with defined [scale] digits after '.'
 * */
fun Double.round(scale: Int = 2): Double {
    val bd = BigDecimal(this)
    return bd.setScale(scale, RoundingMode.HALF_UP).toDouble()
}