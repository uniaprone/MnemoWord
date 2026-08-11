package com.kite.mnemoai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kite.mnemoai.data.local.dao.DayPlanDao
import com.kite.mnemoai.data.local.dao.DayPlanWordDao
import com.kite.mnemoai.data.local.dao.GroupDao
import com.kite.mnemoai.data.local.dao.ReviewWordDao
import com.kite.mnemoai.data.local.dao.WordDao
import com.kite.mnemoai.data.local.dao.WordExtractDao
import com.kite.mnemoai.data.local.dao.WordGroupDao
import com.kite.mnemoai.data.local.entity.DayPlanEntity
import com.kite.mnemoai.data.local.entity.DayPlanWordEntity
import com.kite.mnemoai.data.local.entity.GroupEntity
import com.kite.mnemoai.data.local.entity.ReviewWordEntity
import com.kite.mnemoai.data.local.entity.WordEntity
import com.kite.mnemoai.data.local.entity.WordExtractEntity
import com.kite.mnemoai.data.local.entity.WordFormEntity
import com.kite.mnemoai.data.local.entity.WordGroupEntity
import com.kite.mnemoai.data.local.entity.WordMeaningEntity
import com.kite.mnemoai.data.local.entity.WordPosEntity

@Database(
    entities = [
        GroupEntity::class,
        WordEntity::class,
        WordExtractEntity::class,
        WordGroupEntity::class,
        ReviewWordEntity::class,
        DayPlanEntity::class,
        DayPlanWordEntity::class,
        WordPosEntity::class,
        WordMeaningEntity::class,
        WordFormEntity::class
    ], version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun wordDao(): WordDao
    abstract fun wordGroupDao(): WordGroupDao
    abstract fun wordExtractDao(): WordExtractDao
    abstract fun reviewWordDao(): ReviewWordDao
    abstract fun dayPlanDao(): DayPlanDao
    abstract fun dayPlanWordDao(): DayPlanWordDao
}
