package com.kite.mnemoai.data.network

import com.kite.mnemoai.data.network.deepseek.WordExtractDeepSeekDataSource
import kotlin.concurrent.Volatile

object AiServiceProvider {
    @Volatile
    private var currentDataSource: WordExtractDataSource = WordExtractDeepSeekDataSource()
    public fun switchTo(aiServiceType: AiServiceType){
        when(aiServiceType){
            AiServiceType.DEEPSEEK -> WordExtractDeepSeekDataSource()
        }
    }

    fun getDataSource(): WordExtractDataSource = currentDataSource
}