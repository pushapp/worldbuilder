package com.worldbuilder.mapgame.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import com.worldbuilder.mapgame.Animal
import com.worldbuilder.mapgame.Lifeforms
import com.worldbuilder.mapgame.MapGenerator
import com.worldbuilder.mapgame.Plant
import com.worldbuilder.mapgame.Tile
import com.worldbuilder.mapgame.World
import com.worldbuilder.mapgame.models.CreateWorldParams
import com.worldbuilder.mapgame.models.ExecutionResult
import com.worldbuilder.mapgame.models.Position
import com.worldbuilder.mapgame.models.lifeform.LifeformChangeListener
import com.worldbuilder.mapgame.models.lifeform.LifeformType
import com.worldbuilder.mapgame.repositories.SessionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class GameViewModel(private val repo: SessionRepository) : ViewModel() {

    private val mapGenerator = MapGenerator()
    private var tilemap: Array<Array<Tile>>? = null
    private var lifeFormID = 1

    private var lifeformChangeListener: LifeformChangeListener? = null

    private val refreshIntervalMs = 1000L // 1 second
    private val stepsPerUpdate = 1

    private var timerJob: Job? = null

    //API
    private val _time = MutableLiveData(0L)
    val time: LiveData<Long> = _time

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _bitmap = MutableLiveData<Bitmap>()
    val bitmap: LiveData<Bitmap> = _bitmap

    private val _world = MutableLiveData<World>()
    val world: LiveData<World> = _world

    private val _darwinPoints = MutableLiveData(0)
    val darwinPoints: LiveData<Int> = _darwinPoints

    val errorMessage = LiveEvent<String>()

    private val ticker: Flow<Long> = flow {
        var internalTime = 0L
        while (true) {
            internalTime += 1L
            emit(internalTime)
            delay(refreshIntervalMs)
        }
    }

    //timer section
    fun startTimer() {
        timerJob = viewModelScope.launch {
            ticker.collectLatest {
                if (timerJob?.isActive == false) return@collectLatest

                _time.postValue(it)
                _world.value?.let { w ->
                    w.update(stepsPerUpdate)
                    _darwinPoints.value = w.darwinPoints
                }
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel("Timer has been stopped")
    }

    fun setLifeformChangeListener(listener: LifeformChangeListener?) {
        lifeformChangeListener = listener
    }

    fun onCreateWorldStarted() {
        _loading.value = true
        stopTimer()
    }

    fun onCreateWorldFinished() {
        _loading.value = false
        startTimer()
    }

    @Suppress("unused")
    fun createLifeform(lifeformType: LifeformType, atPositions: List<Position>) {
        lifeFormID++
        _world.value?.apply {
            darwinPoints -= 300
            val plantRes = Lifeforms.getRandomPlantDrawable()
            val animalRes = Lifeforms.getRandomAnimalDrawable()

            for (position1 in atPositions) {
                when (lifeformType) {
                    LifeformType.Plant -> {
                        val plant = createPlant(position1, plantRes)
                        lifeformChangeListener?.onLifeFormCreated(plant)
                        //addLifeformImageView(plant1)
                        addLifeform(plant)
                    }

                    LifeformType.Carnivore, LifeformType.Herbivore -> {
                        val animal = createAnimal(position1, animalRes)
                        animal.foodType = lifeformType
                        lifeformChangeListener?.onLifeFormCreated(animal)
//                        addLifeformImageView(animal)
                        addLifeform(animal)
                    }

                    else -> {}
                }
            }
        }
    }

    @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
    fun createWorld(
        width: Int,
        height: Int,
        waterFrequency: Float,
        mountFrequency: Float
    ) {
        viewModelScope.launch {
            _world.value?.resetLifeforms()

            onCreateWorldStarted()
            val params = CreateWorldParams(width, height, waterFrequency, mountFrequency)
            when (val result = repo.createWorld(params)) {
                is ExecutionResult.Success -> {
                    _world.value = result.data.first!!
                    _bitmap.value = result.data.second!!
                }

                is ExecutionResult.Error -> handleError(result)
            }
            onCreateWorldFinished()
        }
    }

    @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
    fun load() {
        viewModelScope.launch {
            onCreateWorldStarted()

            when (val result = repo.loadTiles()) {
                is ExecutionResult.Success -> tilemap = result.data
                is ExecutionResult.Error -> handleError(result)
            }
            tilemap?.let {
                when (val result = repo.loadWorldBitmap()) {
                    is ExecutionResult.Success -> _bitmap.value = result.data!!
                    is ExecutionResult.Error -> handleError(result)
                }

                when (val result = repo.loadSavedGame(it)) {
                    is ExecutionResult.Success -> {
                        result.data.setLifeformChangeListener(lifeformChangeListener)
                        _world.value = result.data!!
                    }

                    is ExecutionResult.Error -> handleError(result)
                }
                onCreateWorldFinished()
            }
        }
    }

    private fun createPlant(position: Position, plantResId: Int): Plant {
        val name = "plant_$lifeFormID"
        return Plant(
            name, .5f, 50, position, 10, 5, plantResId, 30, lifeFormID
        )
    }

    private fun createAnimal(position: Position, animalResId: Int): Animal {
        val name = "animal_$lifeFormID"
        return Animal(name, 5, .5f, 50, position, 10, animalResId, 30, lifeFormID)
    }

    private fun handleError(result: ExecutionResult.Error) {
        errorMessage.value = result.throwable.localizedMessage
        _loading.value = false
    }
}