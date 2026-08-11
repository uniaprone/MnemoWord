package com.kite.mnemoai.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val maiDispatcher: MaiDispatcher)

enum class MaiDispatcher{
    IO
}
