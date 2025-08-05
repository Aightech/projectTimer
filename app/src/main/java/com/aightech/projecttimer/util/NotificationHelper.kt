// util/NotificationHelper.kt
package com.aightech.projecttimer.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

object NotificationHelper {
    private const val CHANNEL_ID = "timer_channel"

    fun createChannel(ctx: Context) {
        val mgr = ctx.getSystemService(NotificationManager::class.java)
        val chan = NotificationChannel(
            CHANNEL_ID, "Active Timer", NotificationManager.IMPORTANCE_LOW
        )
        mgr.createNotificationChannel(chan)
    }

    fun showOngoing(ctx: Context, title: String, elapsed: Long) {
        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setContentTitle("Working: $title")
            .setContentText("Elapsed: ${elapsed/1000}s")
            .setOngoing(true)
            .build()
        ctx.getSystemService(NotificationManager::class.java).notify(1, notif)
    }

    fun update(ctx: Context, title: String, elapsed: Long) {
        showOngoing(ctx, title, elapsed)
    }

    fun cancel(ctx: Context) {
        ctx.getSystemService(NotificationManager::class.java).cancel(1)
    }
}