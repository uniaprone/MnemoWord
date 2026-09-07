package com.kite.mnemoai.sync

import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kite.mnemoai.notification.MaiNotification
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DailyRemainerWorker @AssistedInject constructor(
    @Assisted context: android.content.Context,
    @Assisted params: WorkerParameters,
    private val maiNotification: MaiNotification
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("DailyRemainerWorker", "执行")
        maiNotification.postReminderNotification()
        return Result.success()
    }
}
