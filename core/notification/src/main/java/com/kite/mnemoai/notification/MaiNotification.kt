package com.kite.mnemoai.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

const val STUDY_REMINDER_CHANNEL_ID = "1"
private const val STUDY_REMINDER_ID = 1001

private const val STUDY_REMINDER_REQUEST_CODE = 0

@Singleton
class MaiNotification @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun postReminderNotification() = with(context) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return@with

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = PendingIntent.getActivity(
            context, STUDY_REMINDER_REQUEST_CODE, launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = createReminderNotification {
            setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.study_reminder_title))
                .setContentText(getString(R.string.study_reminder_content))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        }

        NotificationManagerCompat.from(this).notify(STUDY_REMINDER_ID, notification)
    }
}

private fun Context.createReminderNotification(
    block: NotificationCompat.Builder.() -> Unit
): Notification {
    ensureNotificationChannelExists()
    return NotificationCompat.Builder(this, STUDY_REMINDER_CHANNEL_ID)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .apply(block)
        .build()
}

private fun Context.ensureNotificationChannelExists() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val channel = NotificationChannel(
        STUDY_REMINDER_CHANNEL_ID,
        getString(R.string.study_reminder_channer_name),
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = getString(R.string.study_reminder_channer_describe)
    }

    NotificationManagerCompat.from(this).createNotificationChannel(channel)
}
