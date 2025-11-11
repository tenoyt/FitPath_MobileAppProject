package com.example.fitpath

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("message") ?: "Time for your workout"
        val notification = NotificationCompat.Builder(context, "fitpath_reminders")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("FitPath Reminder")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(1001, notification)
    }
}