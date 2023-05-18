package com.worldbuilder.mapgame.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import com.worldbuilder.mapgame.Lifeform
import com.worldbuilder.mapgame.MapGenerator
import com.worldbuilder.mapgame.Tile
import com.worldbuilder.mapgame.World
import com.worldbuilder.mapgame.models.ExecutionResult
import com.worldbuilder.mapgame.models.lifeform.LifeformChangeListener
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

    private var _world = MutableLiveData<World>()
    val world: LiveData<World> = _world

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
                //mimic some work
                delay(1000)

                when (val result = repo.loadSavedGame(it)) {
                    is ExecutionResult.Success -> _world.value = result.data!!
                    is ExecutionResult.Error -> handleError(result)
                }
                onCreateWorldFinished()
            }
        }
    }

    private fun handleError(result: ExecutionResult.Error) {
        errorMessage.value = result.throwable.localizedMessage
        _loading.value = false
    }
}