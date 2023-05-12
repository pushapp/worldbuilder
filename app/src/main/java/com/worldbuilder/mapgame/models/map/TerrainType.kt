package com.worldbuilder.mapgame.models.map

enum class TerrainType {
    WATER,
    BEACH,
    FOREST,
    GRASSLAND,
    MOUNTAIN,
    DESERT;

    //java like 'static' things should be declared in 'companion object' section in kotlin
    companion object {
        const val WATER_THRESHOLD = 0f
        const val BEACH_THRESHOLD = .1f
        const val FOREST_THRESHOLD = .8f
        const val GRASSLAND_THRESHOLD = 1.6f

        /**
         * converts float value (output from perlin noise) to tile type
         * @param rawValue result of perlin noise function for one "pixel"
         * @return TerrainType based on thresholds defined above
         * */
        fun createFromValue(rawValue: Float): TerrainType {
            // kotlin has extended version of java switch-case
            return when {
                rawValue < TerrainType.WATER_THRESHOLD -> WATER
                rawValue < TerrainType.BEACH_THRESHOLD -> BEACH
                rawValue < TerrainType.FOREST_THRESHOLD -> FOREST
                rawValue < TerrainType.GRASSLAND_THRESHOLD -> GRASSLAND
                else -> MOUNTAIN
            }
        }
    }
}


