package com.kite.mnemoai

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.kite.mnemoai.data.local.UserSetting
import com.kite.mnemoai.data.network.AiServiceProvider
import com.kite.mnemoai.data.repository.AiMnemonicRepository
import com.kite.mnemoai.data.repository.GroupRepository
import com.kite.mnemoai.data.repository.IRepositoryCallback
import com.kite.mnemoai.data.repository.StatisticsRepository
import com.kite.mnemoai.data.repository.UserSettingRepository
import com.kite.mnemoai.data.repository.WordRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {
    @Inject
    lateinit var userSettingRepository: UserSettingRepository

    override fun onCreate() {
        super.onCreate()
        applyDayNightModel()
    }

    private fun applyDayNightModel(){
        userSettingRepository.getUserSetting(object : IRepositoryCallback<UserSetting?> {
            override fun onComplete(userSetting: UserSetting?) {
                if (userSetting == null) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    return
                }
                if (userSetting.lightDarkModel == -1) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                } else if (userSetting.lightDarkModel == 1) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                } else if (userSetting.lightDarkModel == 2) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }

            override fun onError(t: Throwable?) {
            }
        })
    }
}
