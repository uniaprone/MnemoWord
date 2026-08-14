package com.kite.mnemoai.data.repository

import com.kite.mnemoai.common.Dispatcher
import com.kite.mnemoai.common.MaiDispatcher
import com.kite.mnemoai.data.mapper.asExternalModel
import com.kite.mnemoai.data.mapper.asEntity
import com.kite.mnemoai.database.dao.GroupDao
import com.kite.mnemoai.database.dao.WordGroupDao
import com.kite.mnemoai.database.model.WordGroupEntity
import com.kite.mnemoai.model.Result
import com.kite.mnemoai.model.group.AllVocabularyBookItem
import com.kite.mnemoai.model.group.Group
import com.kite.mnemoai.model.group.GroupDetail
import com.kite.mnemoai.model.group.LearningVocabularyBookItem
import com.kite.mnemoai.model.repository.GroupRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val groupDao: GroupDao,
    private val wordGroupDao: WordGroupDao,
    @Dispatcher(MaiDispatcher.IO) private val ioDispatcher: CoroutineDispatcher
) : GroupRepository {

    override fun observeAllGroups(): Flow<Result<List<GroupDetail>>> =
        groupDao.getAllGroupDeatilsLiveData()
            .map { details -> details.map { it.asExternalModel() } }
            .map { Result.Success(it) as Result<List<GroupDetail>> }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(e)) }

    override fun observeGroupById(id: Long): Flow<Result<Group?>> =
        groupDao.getGroupEntityById(id)
            .map { entity -> entity?.asExternalModel() }
            .map { Result.Success(it) as Result<Group?> }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(e)) }

    override fun observeGroupDetailById(id: Long): Flow<Result<List<GroupDetail>>> =
        groupDao.getGroupDetailLiveDataById(id)
            .map { details -> details.map { it.asExternalModel() } }
            .map { Result.Success(it) as Result<List<GroupDetail>> }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(e)) }

    override suspend fun getAllUnlearningGroups(): Result<List<Group>> =
        withContext(ioDispatcher) {
            try {
                Result.Success(groupDao.getAllUnlearningGroupEntities().map { it.asExternalModel() })
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun addLearningGroups(ids: List<Long>) = withContext(ioDispatcher) {
        groupDao.addLearningGroups(ids)
    }

    override suspend fun setVocabularyBookLearningStatus(id: Long, status: Int) = withContext(ioDispatcher) {
        groupDao.setVocabularyBookLearningStatus(id, status)
    }

    override suspend fun addNewOrModifyVocabularyBook(group: Group) = withContext(ioDispatcher) {
        groupDao.insertGroup(group.asEntity())
    }

    override suspend fun modifyVocabularyBook(group: Group) = withContext(ioDispatcher) {
        groupDao.updateGroup(group.asEntity())
    }

    override suspend fun deleteGroup(id: Long) = withContext(ioDispatcher) {
        groupDao.deleteGroupById(id)
    }

    override suspend fun addAlterWords(groupId: Long, ids: List<Long>) = withContext(ioDispatcher) {
        val wordGroupEntities = ids.map { WordGroupEntity(it, groupId) }
        wordGroupDao.insertWordGroups(wordGroupEntities)
    }

    override suspend fun removeAlterWords(groupId: Long, ids: List<Long>) = withContext(ioDispatcher) {
        wordGroupDao.deleteWordGroupItem(groupId, ids)
    }

    override fun observeLearningVocabularyBookItems(): Flow<Result<List<LearningVocabularyBookItem>>> =
        groupDao.getLearningVocabularyBookItem()
            .map { items -> items.map { it.asExternalModel() } }
            .map { Result.Success(it) as Result<List<LearningVocabularyBookItem>> }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(e)) }

    override fun observeAllVocabularyBookItems(): Flow<Result<List<AllVocabularyBookItem>>> =
        groupDao.getAllVocabularyBookItem()
            .map { items -> items.map { it.asExternalModel() } }
            .map { Result.Success(it) as Result<List<AllVocabularyBookItem>> }
            .onStart { emit(Result.Loading) }
            .catch { e -> emit(Result.Error(e)) }
}
