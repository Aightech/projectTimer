// util/NotificationHelper.kt
package com.aightech.projecttimer.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.aightech.projecttimer.R // Assuming your icon is in res/drawable

object NotificationHelper {
    private const val CHANNEL_ID = "timer_channel"
    private const val ONGOING_NOTIFICATION_ID = 1 // Or any other unique integer

    fun createChannel(ctx: Context) {
        val mgr = ctx.getSystemService(NotificationManager::class.java)
        val chan = NotificationChannel(
            CHANNEL_ID, "Active Timer", NotificationManager.IMPORTANCE_LOW
        )
        mgr.createNotificationChannel(chan)
    }

    fun showOngoing(context: Context, contentTitle: String, contentText: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
        // Add other notification properties as needed (e.g., priority, category)

        notificationManager.notify(ONGOING_NOTIFICATION_ID, builder.build())
    }

    // It seems 'elapsed' in the original code was meant to be a String for contentText.
    // If 'elapsed' is truly a Long representing time, you'll need to format it to a String.
    fun update(ctx: Context, title: String, elapsedText: String) { // Changed 'elapsed: Long' to 'elapsedText: String'
        showOngoing(ctx, title, elapsedText)
    }

    fun cancel(ctx: Context) {
        ctx.getSystemService(NotificationManager::class.java).cancel(ONGOING_NOTIFICATION_ID)
    }
}
