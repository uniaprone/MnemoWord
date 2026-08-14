package com.kite.mnemoai.common.di

import android.os.Handler
import android.os.Looper
import androidx.core.os.HandlerCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object HandlerModule {
    @Provides
    fun provideMainThreadHandler(): Handler{
        return HandlerCompat.createAsync(Looper.getMainLooper())
    }
}