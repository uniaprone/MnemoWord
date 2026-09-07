package com.kite.mnemoai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.kite.mnemoai.model.data.ThemeType
import com.kite.mnemoai.model.repository.UserSettingRepository
import com.kite.mnemoai.notification.R
import com.kite.mnemoai.notification.STUDY_REMINDER_CHANNEL_ID
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider{
    @Inject
    lateinit var userSettingRepository: UserSettingRepository
    @Inject lateinit var workerFactory: HiltWorkerFactory

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            applyDayNightModel()
        }

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            STUDY_REMINDER_CHANNEL_ID,
            getString(R.string.study_reminder_channer_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.study_reminder_channer_describe)
        }

        NotificationManagerCompat.from(this).createNotificationChannel(channel)
    }

    private suspend fun applyDayNightModel() {
        val userSetting = userSettingRepository.userSetting.first()
        when (userSetting.lightDarkModel) {
            ThemeType.FOLLOW_SYS -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            ThemeType.DAY -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            ThemeType.NIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()
    }
}
