package com.kite.mnemoai.network.di

import com.kite.mnemoai.network.RetrofitClient
import com.kite.mnemoai.network.MaiNetworkDataSource
import com.kite.mnemoai.network.deepseek.DeepseekRequestFactory
import com.kite.mnemoai.network.NetworkDataSourceImp
import com.kite.mnemoai.network.VoiceDataSource
import com.kite.mnemoai.network.VoiceDataSourceImp
import com.kite.mnemoai.network.YouDaoVoiceClient
import com.kite.mnemoai.network.deepseek.DeepseekService
import com.kite.mnemoai.network.voice.YouDaoVoiceService
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

    @Provides
    fun provideYouDaoVoiceService(): YouDaoVoiceService{
        return YouDaoVoiceClient.youdaoRetrofit.create(
            YouDaoVoiceService::class.java
        )
    }

    @Provides
    fun provideVoiceDataSource(
        service: YouDaoVoiceService
    ): VoiceDataSource{
        return VoiceDataSourceImp(service)
    }
}
