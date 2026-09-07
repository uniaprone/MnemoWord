package com.kite.mnemoai.network.di

import com.kite.mnemoai.network.RetrofitClient
import com.kite.mnemoai.network.MaiNetworkDataSource
import com.kite.mnemoai.network.deepseek.DeepseekRequestFactory
import com.kite.mnemoai.network.NetworkDataSourceImp
import com.kite.mnemoai.network.deepseek.DeepseekService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DeepSeekServiceModule {
    @Provides
    fun provideDeepSeekService(): DeepseekService {
        return RetrofitClient.deepseekRetrofit.create(
            DeepseekService::class.java
        )
    }

    @Provides
    fun provideWordExtractDeepSeekDataSource(
        service: DeepseekService,
        factory: DeepseekRequestFactory
    ): MaiNetworkDataSource {
        return NetworkDataSourceImp(service, factory)
    }
}
