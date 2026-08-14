package com.kite.mnemoai.common.di

import com.kite.mnemoai.common.Dispatcher
import com.kite.mnemoai.common.MaiDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers


@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {
    @Provides
    @Dispatcher(MaiDispatcher.IO)
    fun providesIODispatcher(): CoroutineDispatcher = Dispatchers.IO
}