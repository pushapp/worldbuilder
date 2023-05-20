package com.worldbuilder.mapgame.repositories

import android.content.Context
import android.graphics.Bitmap
import com.worldbuilder.mapgame.MapGenerator
import com.worldbuilder.mapgame.MapUtils
import com.worldbuilder.mapgame.SaveGame
import com.worldbuilder.mapgame.Tile
import com.worldbuilder.mapgame.World
import com.worldbuilder.mapgame.models.CreateWorldParams
import com.worldbuilder.mapgame.models.ExecutionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException

class LocalSessionRepository(private val applicationContext: Context) : SessionRepository {
    override suspend fun loadTiles(): ExecutionResult<Array<Array<Tile>>> {
        return withContext(Dispatchers.IO) {
            try {
                SaveGame.loadTileArrayFromFile(applicationContext)?.let { tiles ->
                    ExecutionResult.Success(tiles)
                } ?: run {
                    ExecutionResult.Error(NullPointerException("no tiles found"))
                }
            } catch (e: Exception) {
                ExecutionResult.Error(NullPointerException("no tiles found"))
            }
        }
    }

    override suspend fun loadWorldBitmap(): ExecutionResult<Bitmap> {
        return withContext(Dispatchers.IO) {
            try {
                SaveGame.loadBitmapFromInternalStorage(applicationContext, SaveGame.BITMAPFILE)
                    ?.let { bitmap ->
                        ExecutionResult.Success(bitmap)
                    } ?: run {
                    ExecutionResult.Error(FileNotFoundException("Can not find map file"))
                }
            } catch (e: Exception) {
                ExecutionResult.Error(e)
            }
        }
    }

    override suspend fun loadSavedGame(map: Array<Array<Tile>>): ExecutionResult<World> {
        return withContext(Dispatchers.IO) {
            try {
                val world = World(map).apply {
                    darwinPoints = SaveGame.loadDarwinFromPrefs(applicationContext)
                    this.animals = SaveGame.loadAnimalsFromPrefs(applicationContext)
                    this.plants = SaveGame.loadPlantsFromPrefs(applicationContext)
                }

                world.animals.forEach { animal ->
                    val pos = animal.position
                    map[pos.x][pos.y].inHabitant = animal
                }

                world.plants.forEach { plant ->
                    val pos = plant.position
                    map[pos.x][pos.y].inHabitant = plant
                }

                for (x in map.indices) {
                    for (y in map[0].indices) {
                        if (map[x][y].inHabitant != null) {
                            val lf = map[x][y].inHabitant
                            world.addLifeform(lf)
                        }
                    }
                }
                ExecutionResult.Success(world)
            } catch (e: Exception) {
                ExecutionResult.Error(e)
            }
        }
    }

    override suspend fun createWorld(params: CreateWorldParams): ExecutionResult<Pair<World, Bitmap>> {
        return withContext(Dispatchers.IO) {
            val mapGenerator = MapGenerator()
            try {
                val rawMap = mapGenerator.generateRandomMap(params)
                val bm = createWorldBitmap(mapGenerator, rawMap, params)

                val map = MapUtils.reduceTileArray(rawMap, MapUtils.tileMapDivisor)
                val world = World(map)

                ExecutionResult.Success(world to bm)
            } catch (e: Exception) {
                ExecutionResult.Error(e)
            }
        }
    }

    private fun createWorldBitmap(
        generator: MapGenerator,
        tiles: Array<Array<Tile>>,
        params: CreateWorldParams
    ): Bitmap = generator.generateRandomMapBitmap(
        params.width,
        params.height,
        Tile.getTileSize(),
        tiles
    ).also { bm ->
        SaveGame.saveBitmapToInternalStorage(applicationContext, bm, SaveGame.BITMAPFILE)
    }
}