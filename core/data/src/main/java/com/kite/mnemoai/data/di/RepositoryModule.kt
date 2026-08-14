package com.kite.mnemoai.data.di

import com.kite.mnemoai.data.repository.AiMnemonicRepositoryImpl
import com.kite.mnemoai.data.repository.GroupRepositoryImpl
import com.kite.mnemoai.data.repository.StatisticsRepositoryImpl
import com.kite.mnemoai.data.repository.UserSettingRepositoryImpl
import com.kite.mnemoai.data.repository.WordRepositoryImpl
import com.kite.mnemoai.model.repository.AiMnemonicRepository
import com.kite.mnemoai.model.repository.GroupRepository
import com.kite.mnemoai.model.repository.StatisticsRepository
import com.kite.mnemoai.model.repository.UserSettingRepository
import com.kite.mnemoai.model.repository.WordRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindWordRepository(impl: WordRepositoryImpl): WordRepository

    @Binds
    abstract fun bindGroupRepository(impl: GroupRepositoryImpl): GroupRepository

    @Binds
    abstract fun bindStatisticsRepository(impl: StatisticsRepositoryImpl): StatisticsRepository

    @Binds
    abstract fun bindUserSettingRepository(impl: UserSettingRepositoryImpl): UserSettingRepository

    @Binds
    abstract fun bindAiMnemonicRepository(impl: AiMnemonicRepositoryImpl): AiMnemonicRepository
}
