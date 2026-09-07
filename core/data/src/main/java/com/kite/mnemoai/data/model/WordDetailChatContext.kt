package com.kite.mnemoai.data.model

import com.kite.mnemoai.model.word.WordDetail

fun WordDetail.toChatContextText(): String = buildString {
    word.word.takeIf { it.isNotBlank() }?.let { appendLine("单词：$it") }
    word.phonetic.takeIf { it.isNotBlank() }?.let { appendLine("音标：$it") }
    val senses = translations.sorted().mapNotNull { translation ->
        val meanings = translation.meanings
            .mapNotNull { it.meaning.takeIf(String::isNotBlank) }
            .joinToString("；")
        meanings.takeIf(String::isNotBlank)?.let { "${translation.pos.pos} $it" }
    }
    if (senses.isNotEmpty()) {
        appendLine("词典释义：")
        senses.forEach { appendLine(it) }
    }
    val formTexts = forms.mapNotNull { it.form.takeIf(String::isNotBlank) }
    if (formTexts.isNotEmpty()) {
        appendLine("词形变化：")
        formTexts.forEach { appendLine(it) }
    }
    extract?.let { wordExtract ->
        wordExtract.explain.takeIf(String::isNotBlank)?.let { appendLine("释义：$it") }
        if (wordExtract.exampleSentences.isNotEmpty()) {
            appendLine("例句：")
            wordExtract.exampleSentences.forEach { example ->
                example.sentence.takeIf(String::isNotBlank)?.let { appendLine(it) }
                example.translation.takeIf(String::isNotBlank)?.let { appendLine("翻译：$it") }
            }
        }
        if (wordExtract.phrases.isNotEmpty()) {
            appendLine("短语：")
            wordExtract.phrases.forEach { phraseItem ->
                val phrase = buildString {
                    phraseItem.phrase.takeIf(String::isNotBlank)?.let { append(it) }
                    phraseItem.meaning.takeIf(String::isNotBlank)?.let { append("（$it）") }
                }
                if (phrase.isNotBlank()) appendLine(phrase)
            }
        }
        wordExtract.affix?.let { affix ->
            val parts = listOfNotNull(affix.prefix, affix.root, affix.suffix)
            if (parts.isNotEmpty()) {
                appendLine("词缀：${parts.joinToString(" ") { "${it.form}${it.meaning}" }}")
            }
        }
    }
}
