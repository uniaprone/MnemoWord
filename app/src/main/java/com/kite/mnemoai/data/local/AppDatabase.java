package com.kite.mnemoai.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.kite.mnemoai.data.local.dao.DayPlanDao;
import com.kite.mnemoai.data.local.dao.DayPlanWordDao;
import com.kite.mnemoai.data.local.dao.GroupDao;
import com.kite.mnemoai.data.local.dao.ReviewWordDao;
import com.kite.mnemoai.data.local.dao.WordDao;
import com.kite.mnemoai.data.local.dao.WordExtractDao;
import com.kite.mnemoai.data.local.dao.WordGroupDao;
import com.kite.mnemoai.data.local.entity.DayPlanEntity;
import com.kite.mnemoai.data.local.entity.DayPlanWordEntity;
import com.kite.mnemoai.data.local.entity.GroupEntity;
import com.kite.mnemoai.data.local.entity.ReviewWordEntity;
import com.kite.mnemoai.data.local.entity.WordEntity;
import com.kite.mnemoai.data.local.entity.WordExtractEntity;
import com.kite.mnemoai.data.local.entity.WordGroupEntity;
import com.kite.mnemoai.data.local.entity.WordMeaningEntity;
import com.kite.mnemoai.data.local.entity.WordPosEntity;

@Database(
        entities = {GroupEntity.class,
                WordEntity.class,
                WordExtractEntity.class,
                WordGroupEntity.class,
                ReviewWordEntity.class,
                DayPlanEntity.class,
                DayPlanWordEntity.class,
                WordPosEntity.class,
                WordMeaningEntity.class},
        version = 1
)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;
    private final static String DATABASE_NAME = "VOCABULARY";

    protected AppDatabase(){};

    public static AppDatabase getInstance(Context context){
        if(INSTANCE == null){
            synchronized(AppDatabase.class){
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    ).createFromAsset("vocabulary.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    public abstract GroupDao groupDao();
    public abstract WordDao wordDao();
    public abstract WordGroupDao wordGroupDao();
    public abstract WordExtractDao wordExtractDao();
    public abstract ReviewWordDao reviewWordDao();
    public abstract DayPlanDao dayPlanDao();
    public abstract DayPlanWordDao dayPlanWordDao();
}
