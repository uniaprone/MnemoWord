package com.kite.mnemoai

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.kite.mnemoai.model.data.ThemeType
import com.kite.mnemoai.model.repository.UserSettingRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {
    @Inject
    lateinit var userSettingRepository: UserSettingRepository

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            applyDayNightModel()
        }
    }

    private suspend fun applyDayNightModel() {
        val userSetting = userSettingRepository.userSetting.first()
        when (userSetting.lightDarkModel) {
            ThemeType.FOLLOW_SYS -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            ThemeType.DAY -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            ThemeType.NIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}
