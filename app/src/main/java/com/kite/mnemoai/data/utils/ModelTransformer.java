package com.kite.mnemoai.data.utils;

import com.google.gson.Gson;
import com.kite.mnemoai.data.local.Converters;
import com.kite.mnemoai.data.local.entity.DayPlanWordEntity;
import com.kite.mnemoai.data.local.entity.GroupEntity;
import com.kite.mnemoai.data.local.entity.ReviewWordEntity;
import com.kite.mnemoai.data.local.entity.WordEntity;
import com.kite.mnemoai.data.local.entity.WordExtractEntity;
import com.kite.mnemoai.data.model.DayPlanWord;
import com.kite.mnemoai.data.model.WordExtract;
import com.kite.mnemoai.data.model.Group;
import com.kite.mnemoai.data.model.Word;
import com.kite.mnemoai.data.model.WordReview;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModelTransformer {
    private static Gson gson = new Gson();
    public static Group transformGroupEntitiesToGroups(GroupEntity groupEntity){
        if(groupEntity == null) return null;
        return new Group(groupEntity.getId(), groupEntity.getName(), groupEntity.getDescription(), groupEntity.isLearning(), groupEntity.getCreateTime());
    }
    public static List<Group> transformGroupEntitiesToGroups(List<GroupEntity> groupEntities){
        if(groupEntities == null) return null;
        return groupEntities.stream()
                .map(groupEntity -> new Group(groupEntity.getId(), groupEntity.getName(), groupEntity.getDescription(), groupEntity.isLearning(), groupEntity.getCreateTime()))
                .collect(Collectors.toList());
    }

    public static Word transformWordEntityToWord(WordEntity wordEntity){
        if(wordEntity == null) return null;
        return new Word(wordEntity.getId(),
                wordEntity.getWord(),
                wordEntity.getPhonetic(),
                wordEntity.getDefinition(),
                wordEntity.getTranslation(),
                wordEntity.getPos(),
                wordEntity.getCollins(),
                wordEntity.getOxford(),
                wordEntity.getTag(),
                wordEntity.getBnc(),
                wordEntity.getFrq(),
                wordEntity.getExchange(),
                wordEntity.getDetail(),
                wordEntity.getAudio());
    }

    public static List<Word> transformWordEntityToWord(List<WordEntity> wordEntities){
        if(wordEntities == null) return null;
        List<Word> words = new ArrayList<>();
        for (WordEntity wordEntity: wordEntities) {
            words.add(new Word(wordEntity.getId(),
                    wordEntity.getWord(),
                    wordEntity.getPhonetic(),
                    wordEntity.getDefinition(),
                    wordEntity.getTranslation(),
                    wordEntity.getPos(),
                    wordEntity.getCollins(),
                    wordEntity.getOxford(),
                    wordEntity.getTag(),
                    wordEntity.getBnc(),
                    wordEntity.getFrq(),
                    wordEntity.getExchange(),
                    wordEntity.getDetail(),
                    wordEntity.getAudio()));
        }
        return words;
    }

    public static WordReview transformWordReviewEntityToWordReview(ReviewWordEntity reviewWordEntity){
        if(reviewWordEntity == null) return null;
        return new WordReview(reviewWordEntity.getWordId(),
                reviewWordEntity.getReviewCount(),
                reviewWordEntity.getReviewCount(),
                reviewWordEntity.getNextReviewTime());
    }

    public static List<DayPlanWord> transformDayPlanWordEntityToDayPlanWord(List<DayPlanWordEntity> dayPlanWordEntities){
        if(dayPlanWordEntities == null) return null;
        return dayPlanWordEntities.stream()
                .map(dayPlanWordEntity ->
                        new DayPlanWord(dayPlanWordEntity.getWordId(),
                                dayPlanWordEntity.getDate(),
                                dayPlanWordEntity.getType(),
                                dayPlanWordEntity.getStatus(),
                                dayPlanWordEntity.getBlurCount(),
                                dayPlanWordEntity.getForgetCount(),
                                dayPlanWordEntity.getLearningTime(),
                                dayPlanWordEntity.getCompleteTime()))
                .collect(Collectors.toList());
    }


//    public static WordExtract transformWordExtractEntityToWordExtract(WordExtractEntity wordExtractEntity){
//        if(wordExtractEntity == null) return null;
//
//        return new WordExtract(wordExtractEntity.getWordId(), Converters.toWordExtract(wordExtractEntity.getExtract()));
//    }

//    public static WordExtract transformWordExtractEntityToWordExtract(WordExtractEntity wordExtractEntity){
//        if(wordExtractEntity == null) return null;
//        return new WordExtract(wordExtractEntity.getWordId(),
//                wordExtractEntity.getWord(),
//                wordExtractEntity.getCoreImage(),
//                wordExtractEntity.getExplain(),
//                wordExtractEntity.getPrefix(),
//                wordExtractEntity.getRoot(),
//                wordExtractEntity.getSuffix());
//    }
}
