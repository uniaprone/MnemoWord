package com.kite.mnemoai.ui.mine

import com.kite.mnemoai.ui.model.LoadingState

class MineUIState(
    val dayNightMode: Int,
    val apikey: String?,
    val apiTestState: LoadingState<String?>?
)
