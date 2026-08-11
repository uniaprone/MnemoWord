package com.kite.mnemoai.data.repository;

import androidx.lifecycle.LiveData;

import com.kite.mnemoai.data.local.dao.GroupDao;
import com.kite.mnemoai.data.local.dao.WordGroupDao;
import com.kite.mnemoai.data.local.entity.GroupEntity;
import com.kite.mnemoai.data.local.entity.WordGroupEntity;
import com.kite.mnemoai.data.model.GroupDetail;
import com.kite.mnemoai.ui.vocabularybook.AllVocabularyBookItem;
import com.kite.mnemoai.ui.vocabularybook.LearningVocabularyBookItem;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GroupRepository {
    private GroupDao groupDao;
    private WordGroupDao wordGroupDao;

    private ExecutorService executors;

    @Inject
    public GroupRepository(ExecutorService executors, GroupDao groupDao, WordGroupDao wordGroupDao) {
        this.executors = executors;
        this.groupDao = groupDao;
        this.wordGroupDao = wordGroupDao;
    }

    public LiveData<List<GroupDetail>> getAllGroupLiveData(){
        return groupDao.getAllGroupDeatilsLiveData();
    };

    public LiveData<GroupEntity> getGroupLiveDataById(long id){
        return groupDao.getGroupEntityById(id);
    }

    public LiveData<List<GroupDetail>> getGroupDetailLiveDataById(long id){
        return groupDao.getGroupDetailLiveDataById(id);
    }

    public void getAllUnlearningGroups(IRepositoryCallback<List<GroupEntity>> callback){
        executors.execute(() -> {
            List<GroupEntity> groupEntities = groupDao.getAllUnlearningGroupEntities();
            callback.onComplete(groupEntities);
        });
    }

    public void addLearningGroups(List<Long> ids){
        executors.execute(() -> {
            groupDao.addLearningGroups(ids);
        });
    }

    public void setVocabularyBookLearningStatus(Long id, int status){
        executors.execute(() -> {
            groupDao.setVocabularyBookLearningStatus(id, status);
        });
    }

    public void addNewOrModifyVocabularyBook(GroupEntity groupEntity){
        executors.execute(() -> groupDao.insertGroup(groupEntity));
    }

    public void modifyVocabularyBook(GroupEntity groupEntity){
        executors.execute(() -> groupDao.updateGroup(groupEntity));
    }

    public void deleteGroup(long id){
        executors.execute(() -> groupDao.deleteGroupById(id));
    }

    public void addAlterWords(long groupId, List<Long> ids){
        executors.execute(() -> {
            List<WordGroupEntity> wordGroupEntities = ids.stream().map(id -> new WordGroupEntity(id, groupId)).collect(Collectors.toList());
            wordGroupDao.insertWordGroups(wordGroupEntities);
        });
    }

    public void removeAlterWords(long groupId, List<Long> ids){
        executors.execute(() -> wordGroupDao.deleteWordGroupItem(groupId, ids));
    }

    public LiveData<List<LearningVocabularyBookItem>> getLearningVocabularyBookItem(){
        return groupDao.getLearningVocabularyBookItem();
    }

    public LiveData<List<AllVocabularyBookItem>> getAllVocabularyBookItem(){
        return groupDao.getAllVocabularyBookItem();
    }
}
