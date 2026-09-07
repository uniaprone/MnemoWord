package com.kite.domain

import com.kite.mnemoai.model.dayplan.ReviewWord
import com.kite.mnemoai.model.repository.ReciteStatistics
import com.kite.mnemoai.model.repository.StatisticsRepository
import jakarta.inject.Inject
import java.time.LocalDate

/**
 * 保存单词背诵信息，更新单词复习记录
 */
class RememberWordUseCase @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) {
    private val masteredReviewCount = 6

    suspend operator fun invoke(reciteStatistics: ReciteStatistics){
        statisticsRepository.saveWordReciteStatistic(reciteStatistics)

        val wordId = reciteStatistics.wordId
        val date = LocalDate.now()

        val reviewWord = statisticsRepository.getReviewWordById(wordId)
        if (reviewWord != null) {
            val reviewCount = reviewWord.reviewCount + 1
            reviewWord.reviewCount = reviewCount
            if (reviewCount >= masteredReviewCount) {
                reviewWord.reviewState = 2
            }
            reviewWord.nextReviewTime = MemoryAlgorithm.calculateNextReviewDate(
                reviewCount,
                reciteStatistics.blurCount,
                reciteStatistics.forgetCount,
                reciteStatistics.learningTime,
                date
            ).toString()
            statisticsRepository.saveReviewWord(reviewWord)
        } else {
            val newReviewWord = ReviewWord(
                wordId, 1, 0,
                MemoryAlgorithm.calculateNextReviewDate(
                    0,
                    reciteStatistics.blurCount,
                    reciteStatistics.forgetCount,
                    reciteStatistics.learningTime,
                    date
                ).toString()
            )
            statisticsRepository.saveReviewWord(newReviewWord)
        }
    }
}