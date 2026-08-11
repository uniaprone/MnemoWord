package com.kite.mnemoai.data.local.di

import android.content.Context
import androidx.room.Room
import com.kite.mnemoai.data.local.AppDatabase
import com.kite.mnemoai.data.local.dao.DayPlanDao
import com.kite.mnemoai.data.local.dao.DayPlanWordDao
import com.kite.mnemoai.data.local.dao.GroupDao
import com.kite.mnemoai.data.local.dao.ReviewWordDao
import com.kite.mnemoai.data.local.dao.WordDao
import com.kite.mnemoai.data.local.dao.WordExtractDao
import com.kite.mnemoai.data.local.dao.WordGroupDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataBaseModule {
    @Provides
    @Singleton
    fun provideAppDataBase(@ApplicationContext context: Context): AppDatabase{
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "VOCABULARY"
        ).createFromAsset("vocabulary.db")
            .build();
    }

    @Provides
    fun provideDayPlanDao(database: AppDatabase): DayPlanDao{
        return database.dayPlanDao()
    }

    @Provides
    fun provideDayPlanWordDao(database: AppDatabase): DayPlanWordDao{
        return database.dayPlanWordDao()
    }

    @Provides
    fun provideReviewWordDao(database: AppDatabase): ReviewWordDao{
        return database.reviewWordDao()
    }

    @Provides
    fun provideWordDao(database: AppDatabase): WordDao{
        return database.wordDao()
    }

    @Provides
    fun provideWordExtractDao(database: AppDatabase): WordExtractDao{
        return database.wordExtractDao()
    }

    @Provides
    fun provideWordGroupDao(database: AppDatabase): WordGroupDao{
        return database.wordGroupDao()
    }

    @Provides
    fun provideGroupDao(database: AppDatabase): GroupDao{
        return database.groupDao()
    }
}