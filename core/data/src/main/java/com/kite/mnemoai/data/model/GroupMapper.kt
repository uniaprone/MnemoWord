package com.kite.mnemoai.data.model

import com.kite.mnemoai.database.model.AllVocabularyBookItem
import com.kite.mnemoai.database.model.DailyStatistic
import com.kite.mnemoai.database.model.DayPlanEntity
import com.kite.mnemoai.database.model.GroupDetail
import com.kite.mnemoai.database.model.GroupEntity
import com.kite.mnemoai.database.model.LearningVocabularyBookItem
import com.kite.mnemoai.database.model.StudyStatistic
import com.kite.mnemoai.model.dayplan.DayPlan
import com.kite.mnemoai.model.group.AllVocabularyBookItem as DomainAllVocabularyBookItem
import com.kite.mnemoai.model.group.Group
import com.kite.mnemoai.model.group.GroupDetail as DomainGroupDetail
import com.kite.mnemoai.model.group.LearningVocabularyBookItem as DomainLearningVocabularyBookItem
import com.kite.mnemoai.model.statistic.DailyStatistic as DomainDailyStatistic
import com.kite.mnemoai.model.statistic.StudyStatistic as DomainStudyStatistic

fun GroupEntity.asExternalModel(): Group = Group(
    id = id,
    name = name,
    description = description ?: "",
    createTime = createTime,
    isLearning = isLearning
)

fun Group.asEntity(): GroupEntity = GroupEntity(
    id,
    name,
    description,
    isLearning,
    createTime
)

fun GroupDetail.asExternalModel(): DomainGroupDetail = DomainGroupDetail(
    group = groupEntity.asExternalModel(),
    totalWords = totalWords,
    learningCount = learningCount,
    reviewingCount = reviewingCount,
    masteredCount = masteredCount
)

fun DayPlanEntity.asExternalModel(): DayPlan = DayPlan(
    date = date,
    learnGoal = learnGole
)

fun DailyStatistic.asExternalModel(): DomainDailyStatistic = DomainDailyStatistic(
    reviewCount = reviewCount,
    learnedCount = learnedCount,
    reviewedCount = reviewedCount
)

fun StudyStatistic.asExternalModel(): DomainStudyStatistic = DomainStudyStatistic(
    date = date,
    reviewedCount = reviewedCount,
    newLearnedCount = newLearnedCount,
    totalLearningTime = totalLearningTime
)

fun AllVocabularyBookItem.asExternalModel(): DomainAllVocabularyBookItem = DomainAllVocabularyBookItem(
    id = id,
    name = name,
    totalWords = totalWords,
    masteredWords = masteredWords
)

fun LearningVocabularyBookItem.asExternalModel(): DomainLearningVocabularyBookItem = DomainLearningVocabularyBookItem(
    id = id,
    name = name,
    desc = desc,
    totalWords = totalWords,
    learningWords = learningWords,
    reviewingWords = reviewingWords,
    masteredWords = masteredWords
)
