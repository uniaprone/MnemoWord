package com.kite.mnemoai.network

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiServiceProvider @Inject constructor(
    private val wordExtractDataSourceImp: WordExtractDataSource
){
    fun getWordExtractDataSourceImp(): WordExtractDataSource{
        return wordExtractDataSourceImp
    }
}