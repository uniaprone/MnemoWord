package com.kite.mnemoai.sync

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.util.concurrent.TimeUnit

object StudyReminderScheduler {
    fun setStudyReminder(context: Context, duration: Duration) {
        val request = PeriodicWorkRequestBuilder<DailyRemainerWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(duration)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            STUDY_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    fun disableStudyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(STUDY_REMINDER_WORK_NAME)
    }
}

internal const val STUDY_REMINDER_WORK_NAME = "StudyReminderWorkName"
