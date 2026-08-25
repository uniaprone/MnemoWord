package com.kite.mnemoai.model.repository

import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.word.WordDetail
import com.kite.mnemoai.model.word.WordItem
import kotlinx.coroutines.flow.Flow

interface WordRepository {
    suspend fun addTodayNewLearningWordEntities(count: Int, date: String, excludeWordIds: List<Long>): List<Long>
    suspend fun selectTodayReviewingWordEntities(date: String): List<Long>
    fun observeWordListByGroupId(groupId: Long): Flow<Result<List<WordItem>>>

    fun observeDailyReciteStatus(): Flow<Result<Int>>

    fun observeUnfinishedWordDetailInfos(): Flow<Result<List<WordDetail>>>

    fun observeWordDetailById(id: Long): Flow<Result<WordDetail?>>

    suspend fun searchWord(word: String): Result<List<WordItem>>

    suspend fun addOptionWordsSearch(groupId: Long, word: String): Result<List<WordItem>>

    suspend fun removeOptionWordsSearch(groupId: Long, word: String): Result<List<WordItem>>
}
