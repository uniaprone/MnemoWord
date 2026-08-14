package com.kite.mnemoai

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.kite.mnemoai.model.repository.UserSettingRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        val userSetting = userSettingRepository.getUserSettingSync()
        when (userSetting.lightDarkModel) {
            -1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}
