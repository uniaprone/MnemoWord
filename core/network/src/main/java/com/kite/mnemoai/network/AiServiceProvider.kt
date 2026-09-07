package com.kite.mnemoai.network

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiServiceProvider @Inject constructor(
    private val maiNetworkDataSourceImp: MaiNetworkDataSource
){
    fun getNetworkDataSourceImp(): MaiNetworkDataSource{
        return maiNetworkDataSourceImp
    }
}