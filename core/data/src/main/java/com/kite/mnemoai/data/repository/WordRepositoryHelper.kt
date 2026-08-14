package com.kite.mnemoai.data.repository

import com.kite.mnemoai.database.dao.DayPlanWordDao
import com.kite.mnemoai.database.dao.WordDao
import com.kite.mnemoai.database.model.DayPlanWordEntity
import com.kite.mnemoai.database.model.WordEntity
import java.time.LocalDate

internal object WordRepositoryHelper {
    private val lock = Any()

    fun setDailyDayPlanWordEntities(
        learningCount: Int,
        wordDao: WordDao,
        dayPlanWordDao: DayPlanWordDao
    ) {
        synchronized(lock) {
            val date = LocalDate.now().toString()
            val dayPlanWordEntities = dayPlanWordDao.queryDayPlanWordsByDate(date)
            if (dayPlanWordEntities.isEmpty()) {
                val newLearningWordEntities = wordDao.selectTodayNewLearningWordEntities(learningCount, date)
                val reviewingWordEntities = wordDao.selectTodayReviewingWordEntities(date)
                val allDayPlanWordEntities = (newLearningWordEntities + reviewingWordEntities).map { entity ->
                    DayPlanWordEntity(
                        entity.id, date,
                        if (entity in newLearningWordEntities) 0 else 1,
                        0, 0, 0, 0, null
                    )
                }
                dayPlanWordDao.InsertDayPlanWord(allDayPlanWordEntities)
            }
            val newLearningWordIds = dayPlanWordDao.getTodayNewLearningWordsId(date)
            if (newLearningWordIds.size < learningCount) {
                val newLearningWordEntities = wordDao.addTodayNewLearningWordEntities(
                    learningCount, date, newLearningWordIds
                )
                dayPlanWordDao.InsertDayPlanWord(
                    newLearningWordEntities.map { entity ->
                        DayPlanWordEntity(entity.id, date, 0, 0, 0, 0, 0, null)
                    }
                )
            }
        }
    }
}
