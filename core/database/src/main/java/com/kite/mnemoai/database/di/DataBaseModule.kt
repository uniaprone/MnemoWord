package com.kite.mnemoai.database.di

import android.content.Context
import androidx.room.Room
import com.kite.mnemoai.database.AppDatabase
import com.kite.mnemoai.database.dao.DayPlanDao
import com.kite.mnemoai.database.dao.DayPlanWordDao
import com.kite.mnemoai.database.dao.GroupDao
import com.kite.mnemoai.database.dao.ReviewWordDao
import com.kite.mnemoai.database.dao.WordDao
import com.kite.mnemoai.database.dao.WordExtractDao
import com.kite.mnemoai.database.dao.WordGroupDao
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
    fun provideAppDataBase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "VOCABULARY"
        ).createFromAsset("vocabulary.db")
            .build();
    }

    @Provides
    fun provideDayPlanDao(database: AppDatabase): DayPlanDao {
        return database.dayPlanDao()
    }

    @Provides
    fun provideDayPlanWordDao(database: AppDatabase): DayPlanWordDao {
        return database.dayPlanWordDao()
    }

    @Provides
    fun provideReviewWordDao(database: AppDatabase): ReviewWordDao {
        return database.reviewWordDao()
    }

    @Provides
    fun provideWordDao(database: AppDatabase): WordDao {
        return database.wordDao()
    }

    @Provides
    fun provideWordExtractDao(database: AppDatabase): WordExtractDao {
        return database.wordExtractDao()
    }

    @Provides
    fun provideWordGroupDao(database: AppDatabase): WordGroupDao {
        return database.wordGroupDao()
    }

    @Provides
    fun provideGroupDao(database: AppDatabase): GroupDao {
        return database.groupDao()
    }
}