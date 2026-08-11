package com.kite.mnemoai.data.network.di

import com.kite.mnemoai.data.network.RetrofitClient
import com.kite.mnemoai.data.network.WordExtractDataSource
import com.kite.mnemoai.data.network.deepseek.WordExtractDeepSeekDataSource
import com.kite.mnemoai.data.network.deepseek.WordExtractDeepSeekService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object WordExtractDeepSeekServiceModule {
    @Provides
    fun provideWordExtractDeepSeekService(): WordExtractDeepSeekService{
        return RetrofitClient.deepseekRetrofit.create(
            WordExtractDeepSeekService::class.java
        )
    }

    @Provides
    fun provideWordExtractDeepSeekDataSource(
        service: WordExtractDeepSeekService
    ): WordExtractDataSource{
        return WordExtractDeepSeekDataSource(service)
    }
}