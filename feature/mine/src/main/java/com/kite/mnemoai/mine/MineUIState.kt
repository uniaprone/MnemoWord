package com.kite.mnemoai.mine

import com.kite.mnemoai.model.Result

class MineUIState(
    val dayNightMode: Int,
    val apikey: String?,
    val apiTestState: Result<String>?
)
