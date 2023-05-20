package com.worldbuilder.mapgame.models

data class CreateWorldParams(
    val width: Int,
    val height: Int,
    val waterFrequency: Float,
    val mountainFrequency: Float
)