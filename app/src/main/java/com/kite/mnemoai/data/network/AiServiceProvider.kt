package com.kite.mnemoai.data.network

import com.kite.mnemoai.data.network.deepseek.WordExtractDeepSeekDataSource
import com.kite.mnemoai.data.repository.UserSettingRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.Volatile

@Singleton
class AiServiceProvider @Inject constructor(
    private val wordExtractDataSourceImp: WordExtractDataSource
){
    fun getWordExtractDataSourceImp(): WordExtractDataSource{
        return wordExtractDataSourceImp
    }
}