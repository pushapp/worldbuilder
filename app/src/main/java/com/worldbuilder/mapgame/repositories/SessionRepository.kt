package com.worldbuilder.mapgame.repositories

import android.graphics.Bitmap
import com.worldbuilder.mapgame.Tile
import com.worldbuilder.mapgame.World
import com.worldbuilder.mapgame.models.ExecutionResult

interface SessionRepository {
    suspend fun loadTiles(): ExecutionResult<Array<Array<Tile>>>
    suspend fun loadWorldBitmap(): ExecutionResult<Bitmap>
    suspend fun loadSavedGame(map: Array<Array<Tile>>): ExecutionResult<World>
}