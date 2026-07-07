package com.kite.mnemoai.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.kite.mnemoai.MainApplication;
import com.kite.mnemoai.data.local.AppDatabase;
import com.kite.mnemoai.data.local.dao.GroupDao;
import com.kite.mnemoai.data.local.entity.GroupEntity;
import com.kite.mnemoai.data.model.Group;
import com.kite.mnemoai.data.model.GroupDetail;
import com.kite.mnemoai.data.utils.ModelTransformer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Handler;

public class GroupRepository {
    private AppDatabase db;
    private GroupDao groupDao;

    private ExecutorService executors;
    private Handler handler;

    public GroupRepository(Application app) {
        this.executors = MainApplication.getEXECUTOR_SERVICE();
        this.db = ((MainApplication) app).getAppDatabase();
        this.groupDao = db.groupDao();
    }

    public LiveData<List<GroupDetail>> getAllGroupLiveData(){
        return groupDao.getAllGroupDeatilsLiveData();
    };

    public LiveData<Group> getGroupLiveDataById(long id){
        return Transformations.map(groupDao.getGroupEntityById(id), ModelTransformer::transformGroupEntitiesToGroups);
    }

    public LiveData<List<GroupDetail>> getGroupDetailLiveDataById(long id){
        return groupDao.getGroupDetailLiveDataById(id);
    }

    public void getAllUnlearningGroups(IRepositoryCallback<List<Group>> callback){
        executors.execute(() -> {
            List<GroupEntity> groupEntities = groupDao.getAllUnlearningGroupEntities();
            callback.onComplete(ModelTransformer.transformGroupEntitiesToGroups(groupEntities));
        });
    }

    public void addLearningGroups(List<Long> ids){
        executors.execute(() -> {
            groupDao.addLearningGroups(ids);
        });
    }
}
