package com.kite.mnemoai.data.model

import com.kite.mnemoai.model.data.AiProvider
import com.kite.mnemoai.network.AiServiceType

fun AiProvider.asNetWorkModel() = when(this){
    AiProvider.DEEPSEEK -> AiServiceType.DEEPSEEK
}