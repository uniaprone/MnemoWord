package com.kite.mnemoai.model.repository

import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.group.AllVocabularyBookItem
import com.kite.mnemoai.model.group.Group
import com.kite.mnemoai.model.group.GroupDetail
import com.kite.mnemoai.model.group.LearningVocabularyBookItem
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun observeAllGroups(): Flow<Result<List<GroupDetail>>>

    fun observeGroupById(id: Long): Flow<Result<Group?>>

    fun observeGroupDetailById(id: Long): Flow<Result<List<GroupDetail>>>

    suspend fun getAllUnlearningGroups(): Result<List<Group>>

    suspend fun addLearningGroups(ids: List<Long>)

    suspend fun setVocabularyBookLearningStatus(id: Long, status: Int)

    suspend fun addNewOrModifyVocabularyBook(group: Group)

    suspend fun modifyVocabularyBook(group: Group)

    suspend fun deleteGroup(id: Long)

    suspend fun addAlterWords(groupId: Long, ids: List<Long>)

    suspend fun removeAlterWords(groupId: Long, ids: List<Long>)

    fun observeLearningVocabularyBookItems(): Flow<Result<List<LearningVocabularyBookItem>>>

    fun observeAllVocabularyBookItems(): Flow<Result<List<AllVocabularyBookItem>>>
}
