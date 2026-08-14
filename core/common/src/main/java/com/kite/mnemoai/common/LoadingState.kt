package com.kite.mnemoai.common

sealed class LoadingState<out T> {
    object Loading: LoadingState<Nothing>()
    data class Success<T>(val data: T): LoadingState<T>()
    data class Error(val exception: Throwable): LoadingState<Nothing>()
}