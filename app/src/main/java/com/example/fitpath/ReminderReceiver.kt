package com.example.fitpath

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("message") ?: "Time for your workout"

        // Step 1: Build and show the notification (your original code, which is correct)
        val notification = NotificationCompat.Builder(context, "fitpath_reminders")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("FitPath Reminder")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(1001, notification)

        // Step 2: NEW - Re-schedule the alarm for the next day
        rescheduleNextAlarm(context)
    }

    private fun rescheduleNextAlarm(context: Context) {
        // Read the user's saved preferences to get the desired time
        val prefs = Prefs(context)
        if (!prefs.getRemindersEnabled()) {
            // If the user has disabled reminders since it was last set, do nothing.
            return
        }
        val hour = prefs.getReminderHour()
        val minute = prefs.getReminderMinute()

        // Re-create the same Intent and PendingIntent
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("message", "Remember to log your workout today")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            2001, // Must be the same request code used in SettingsFragment
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // If we don't have permission (e.g., user revoked it), we can't reschedule.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                return
            }
        }

        // Calculate the time for the next day
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            // Crucially, add one day to the current date to schedule for tomorrow
            add(Calendar.DAY_OF_YEAR, 1)
        }

        // Set the exact alarm for the next day
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}
