package com.worldbuilder.mapgame.viewmodels

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import com.worldbuilder.mapgame.Lifeform
import com.worldbuilder.mapgame.MapGenerator
import com.worldbuilder.mapgame.SaveGame
import com.worldbuilder.mapgame.Tile
import com.worldbuilder.mapgame.World
import com.worldbuilder.mapgame.models.ExecutionResult
import com.worldbuilder.mapgame.models.lifeform.LifeformChangeListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import kotlin.Exception
import kotlin.NullPointerException

class GameViewModel : ViewModel() {

    private val mapGenerator = MapGenerator()
    private var world: World? = null
    private var tilemap: Array<Array<Tile>>? = null
    private val lifeFormID = 1

    private val lifeformChangeListener: LifeformChangeListener

    private val refreshIntervalMs = 1000L // 1 second

    private var timerJob: Job? = null

    //API
    private val _time = MutableLiveData(0L)
    val time: LiveData<Long> = _time

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _bitmap = MutableLiveData<Bitmap>()
    val bitmap: LiveData<Bitmap> = _bitmap

    val errorMessage = LiveEvent<String>()

    private val ticker: Flow<Long> = flow {
        var internalTime = 0L
        while (true) {
            internalTime += 1L
            emit(internalTime)
            delay(refreshIntervalMs)
        }
    }

    init {
        lifeformChangeListener = object : LifeformChangeListener {
            override fun onLifeFormCreated(lifeform: Lifeform?) {
                //TODO: complete
            }

            override fun onLifeformRemoved(lifeform: Lifeform?) {
                //TODO: complete
            }

        }
    }

    //timer section
    fun startTimer() {
        timerJob = viewModelScope.launch {
            ticker.collectLatest {
                _time.postValue(it)
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel("Timer has been stopped")
    }

    fun onCreateWorldStarted() {
        _loading.value = true
        stopTimer()
    }

    fun onCreateWorldFinished() {
        _loading.value = false
        startTimer()
    }

    fun load(context: Context) {
        viewModelScope.launch {
            onCreateWorldStarted()

            when (val result = loadTiles(context)) {
                is ExecutionResult.Success -> tilemap = result.data
                is ExecutionResult.Error -> handleError(result)
            }
            tilemap?.let {
                @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
                when(val result = loadWorldBitmap(context)) {
                    is ExecutionResult.Success -> _bitmap.value = result.data!!
                    is ExecutionResult.Error -> handleError(result)
                }
                //mimic some work
                delay(1000)

                //TODO: move to repository
                loadSavedGame(context, tilemap!!)
                //throw Exception("some error happened")
                onCreateWorldFinished()
            }
        }
    }

    private fun handleError(result: ExecutionResult.Error) {
        errorMessage.value = result.throwable.localizedMessage
        _loading.value = false
    }

    //TODO: move to repository
    private suspend fun loadTiles(context: Context): ExecutionResult<Array<Array<Tile>>> {
        return withContext(Dispatchers.IO) {
            try {
                SaveGame.loadTileArrayFromFile(context)?.let { tiles ->
                    ExecutionResult.Success(tiles)
                } ?: run {
                    ExecutionResult.Error(NullPointerException("no tiles found"))
                }
            } catch (e: Exception) {
                ExecutionResult.Error(NullPointerException("no tiles found"))
            }
        }
    }

    //TODO: move to repository
    private suspend fun loadWorldBitmap(context: Context): ExecutionResult<Bitmap> {
        return withContext(Dispatchers.IO) {
            try {
                SaveGame.loadBitmapFromInternalStorage(context, SaveGame.BITMAPFILE)
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

    //TODO: move to repository
    private fun loadSavedGame(context: Context, map: Array<Array<Tile>>) {
        World(map, lifeformChangeListener).also {
            val animals = SaveGame.loadAnimalsFromPrefs(context)
            for (animal in animals) {
                val pos = animal.position
                map[pos.x][pos.y].inHabitant = animal
                //addLifeformImageView(animal)
            }
            val plants = SaveGame.loadPlantsFromPrefs(context)
            for (plant in plants) {
                val pos = plant.position
                map[pos.x][pos.y].inHabitant = plant
                //addLifeformImageView(plant)
            }

            it.darwinPoints = SaveGame.loadDarwinFromPrefs(context)
            it.animals = animals
            it.plants = plants

            for (x in map.indices) {
                for (y in map[0].indices) {
                    if (map[x][y].inHabitant != null) {
                        val lf = map[x][y].inHabitant
                        it.addLifeform(lf)
//                    addLifeformImageView(lf)
                    }
                }
            }
            world = it
        }
    }
}