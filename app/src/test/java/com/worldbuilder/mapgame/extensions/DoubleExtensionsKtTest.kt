package com.worldbuilder.mapgame.extensions

import junit.framework.TestCase
import org.junit.Test

internal class DoubleExtensionsKtTest {

    @Test
    fun `is round for 1,456 (scale=2) returns 1,46`() {
        val number = 1.456
        TestCase.assertEquals(1.46, number.round(2))
    }

    @Test
    fun `is round for 1,453 (scale=2) returns 1,45`() {
        val number = 1.453
        TestCase.assertEquals(1.45, number.round(2))
    }

    @Test
    fun `is round for 1,0 (scale=2) returns same value`() {
        val number = 1.0
        TestCase.assertEquals(1.0, number.round(2))
    }
}