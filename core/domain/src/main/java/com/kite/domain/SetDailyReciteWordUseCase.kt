package com.kite.domain

import com.kite.mnemoai.model.dayplan.DayPlanWord
import com.kite.mnemoai.model.repository.StatisticsRepository
import com.kite.mnemoai.model.repository.UserSettingRepository
import com.kite.mnemoai.model.repository.WordRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class SetDailyReciteWordUseCase @Inject constructor(
    private val wordRepository: WordRepository,
    private val statisticsRepository: StatisticsRepository,
    private val userSettingRepository: UserSettingRepository
) {
    suspend operator fun invoke() {
        val date = LocalDate.now().toString()
        val dayPlanWords = statisticsRepository.getDayPlanWordsByDate(date)
        val newLearningWords = dayPlanWords.filter { it.type == 0 }
        val newLearningFinishedWord = newLearningWords.filter { it.status == 1 }
        val newLearningWordCount = userSettingRepository.userSetting.first().newLearningWordCount

        val reviewWordIds = wordRepository.selectTodayReviewingWordEntities(date)
        val reviewDayPlanWords = reviewWordIds.map { reviewWord ->
            DayPlanWord(
                reviewWord, date,
                1, 0,
                0, 0,
                0, null
            )
        }
        statisticsRepository.setReviewDayPlanWords(reviewDayPlanWords)

        if (newLearningWords.size < newLearningWordCount) {
            val newLearningWordIds = wordRepository.addTodayNewLearningWordEntities(
                newLearningWordCount - newLearningWords.size,
                date,
                dayPlanWords.map { it.wordId }
            )
            val dayPlanWords = newLearningWordIds.map { newLearningWordId ->
                DayPlanWord(
                    newLearningWordId, date,
                    0, 0,
                    0, 0,
                    0, null
                )
            }
            statisticsRepository.addNewLearningDayPlanWords(dayPlanWords)
        } else if (newLearningWords.size > newLearningWordCount) {
            if (newLearningFinishedWord.size < newLearningWordCount) {
                statisticsRepository.deleteExceedDayPlanWords(
                    newLearningWords.size - newLearningWordCount
                )
            }
        }

    }
}