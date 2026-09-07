package com.kite.mnemoai.data.model

import com.kite.mnemoai.database.model.WordDetailInfo
import com.kite.mnemoai.database.model.WordEntity
import com.kite.mnemoai.database.model.WordPosEntity
import com.kite.mnemoai.database.model.WordMeaningEntity
import com.kite.mnemoai.database.model.WordFormEntity
import com.kite.mnemoai.database.model.WordTranslation as DbWordTranslation
import com.kite.mnemoai.database.model.WordExtract as DbWordExtract
import com.kite.mnemoai.database.model.Phrase as DbPhrase
import com.kite.mnemoai.database.model.ExampleSentence as DbExampleSentence
import com.kite.mnemoai.database.model.Affix as DbAffix
import com.kite.mnemoai.database.model.AffixPart as DbAffixPart
import com.kite.mnemoai.database.model.DayPlanWordEntity
import com.kite.mnemoai.database.model.ReviewWordEntity
import com.kite.mnemoai.model.word.Word
import com.kite.mnemoai.model.word.WordPos
import com.kite.mnemoai.model.word.WordMeaning
import com.kite.mnemoai.model.word.WordForm
import com.kite.mnemoai.model.word.WordTranslation
import com.kite.mnemoai.model.word.WordExtract
import com.kite.mnemoai.model.word.Phrase
import com.kite.mnemoai.model.word.ExampleSentence
import com.kite.mnemoai.model.word.Affix
import com.kite.mnemoai.model.word.AffixPart
import com.kite.mnemoai.model.dayplan.DayPlanWord
import com.kite.mnemoai.model.dayplan.ReviewWord
import com.kite.mnemoai.model.word.WordDetail

fun WordEntity.asExternalModel(): Word = Word(
    id = id,
    word = word,
    phonetic = phonetic ?: "",
    definition = definition ?: "",
    translation = translation ?: "",
    pos = pos ?: "",
    collins = collins,
    oxford = oxford,
    tag = tag ?: "",
    bnc = bnc,
    frq = frq,
    exchange = exchange ?: "",
    detail = detail ?: "",
    audio = audio ?: ""
)

fun WordPosEntity.asExternalModel(): WordPos = WordPos(
    id = id,
    wordId = wordId,
    pos = pos
)

fun WordMeaningEntity.asExternalModel(): WordMeaning = WordMeaning(
    id = id,
    posId = posId,
    meaning = meaning
)

fun WordFormEntity.asExternalModel(): WordForm = WordForm(
    id = id,
    wordId = wordId,
    typeCode = typeCode,
    form = form
)

fun DbWordTranslation.asExternalModel(): WordTranslation = WordTranslation(
    pos = wordPos.asExternalModel(),
    meanings = wordMeanings.map { it.asExternalModel() }
)

fun DbWordExtract.asExternalModel(): WordExtract = WordExtract(
    word = word,
    phrases = phrase.map { it.asExternalModel() },
    exampleSentences = exampleSentence.map { it.asExternalModel() },
    affix = affix?.asExternalModel(),
    explain = explain
)

fun DbPhrase.asExternalModel(): Phrase = Phrase(
    phrase = phrase,
    meaning = meaning
)

fun DbExampleSentence.asExternalModel(): ExampleSentence = ExampleSentence(
    sentence = sentence,
    translation = translation
)

fun DbAffix.asExternalModel(): Affix = Affix(
    prefix = prefix?.asExternalModel(),
    root = root?.asExternalModel(),
    suffix = suffix?.asExternalModel()
)

fun DbAffixPart.asExternalModel(): AffixPart = AffixPart(
    form = form,
    meaning = meaning
)

fun DayPlanWordEntity.asExternalModel(): DayPlanWord = DayPlanWord(
    wordId = wordId,
    date = date,
    type = type,
    status = status,
    blurCount = blurCount,
    forgetCount = forgetCount,
    learningTime = learningTime,
    completeTime = completeTime
)

fun WordDetailInfo.asExternalModel(): WordDetail = WordDetail(
    word = wordEntity.asExternalModel(),
    translations = wordTranslation.map { it.asExternalModel() },
    forms = wordForm.map { it.asExternalModel() },
    extract = wordExtractEntity?.extract?.asExternalModel(),
    dayPlanWords = dayPlanWordEntities.map { it.asExternalModel() }
)

fun ReviewWord.asEntity() = ReviewWordEntity(
    wordId = wordId,
    reviewState = reviewState,
    reviewCount = reviewCount,
    nextReviewTime = nextReviewTime
)

fun DayPlanWord.asEntity() = DayPlanWordEntity(
    wordId = wordId,
    date = date,
    type = type,
    status = status,
    blurCount = blurCount,
    forgetCount = forgetCount,
    learningTime = learningTime,
    completeTime = completeTime
)


