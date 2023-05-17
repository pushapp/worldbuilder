package com.worldbuilder.mapgame.models

sealed class ExecutionResult<out R> {
    data class Success<out T>(val data: T) : ExecutionResult<T>()
    data class Error(val throwable: Throwable) : ExecutionResult<Nothing>()
}