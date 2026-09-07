package com.kite.mnemoai.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kite.mnemoai.database.dao.ChatMessageDao
import com.kite.mnemoai.database.dao.DayPlanDao
import com.kite.mnemoai.database.dao.DayPlanWordDao
import com.kite.mnemoai.database.dao.GroupDao
import com.kite.mnemoai.database.dao.ReviewWordDao
import com.kite.mnemoai.database.dao.WordDao
import com.kite.mnemoai.database.dao.WordExtractDao
import com.kite.mnemoai.database.dao.WordGroupDao
import com.kite.mnemoai.database.model.ChatMessageEntity
import com.kite.mnemoai.database.model.DayPlanEntity
import com.kite.mnemoai.database.model.DayPlanWordEntity
import com.kite.mnemoai.database.model.GroupEntity
import com.kite.mnemoai.database.model.ReviewWordEntity
import com.kite.mnemoai.database.model.WordEntity
import com.kite.mnemoai.database.model.WordExtractEntity
import com.kite.mnemoai.database.model.WordFormEntity
import com.kite.mnemoai.database.model.WordGroupEntity
import com.kite.mnemoai.database.model.WordMeaningEntity
import com.kite.mnemoai.database.model.WordPosEntity

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
        WordFormEntity::class,
        ChatMessageEntity::class
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
    abstract fun chaMessageDao(): ChatMessageDao
}
