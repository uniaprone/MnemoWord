package com.kite.mnemoai.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Module
@InstallIn(SingletonComponent::class)
object ExecutorServiceModule {
    @Provides
    fun providesExecutorService(): ExecutorService {
        return Executors.newFixedThreadPool(4)
    }
}