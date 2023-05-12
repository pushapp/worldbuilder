package com.worldbuilder.mapgame.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class GameViewModel: ViewModel() {

    private val refreshIntervalMs = 1000L // 1 second

    private var timerJob: Job? = null
    private val _time = MutableLiveData(0L)
    val time: LiveData<Long> = _time

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

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
}