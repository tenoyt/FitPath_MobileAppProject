package com.example.fitpath

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog // Import TimePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button // Import Button
import android.widget.Switch
import android.widget.TextView // Import TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import java.util.Locale // Import Locale

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var prefs: Prefs
    private lateinit var remindersSwitch: Switch
    private lateinit var themeSwitch: Switch

    // NEW: References for the actual UI components
    private lateinit var reminderTimeText: TextView
    private lateinit var pickTimeButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = Prefs(requireContext())

        // Correctly find views by their IDs from your XML
        remindersSwitch = view.findViewById(R.id.switchReminders)
        themeSwitch = view.findViewById(R.id.switchTheme)
        reminderTimeText = view.findViewById(R.id.tvReminderTime)
        pickTimeButton = view.findViewById(R.id.btnPickTime)

        setupThemeSwitch()
        setupReminderControls()
    }

    private fun setupThemeSwitch() {
        themeSwitch.isChecked = prefs.darkMode
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.darkMode = isChecked
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            activity?.recreate()
        }
    }

    // --- THIS IS THE COMPLETELY REWRITTEN AND CORRECTED FUNCTION ---
    private fun setupReminderControls() {
        // 1. Set initial states from preferences
        remindersSwitch.isChecked = prefs.getRemindersEnabled()
        updateReminderTimeText()

        // 2. Listener for the enable/disable switch
        remindersSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.setRemindersEnabled(isChecked)
            if (isChecked) {
                // When toggled ON, schedule the alarm with the currently saved time
                scheduleReminder(prefs.getReminderHour(), prefs.getReminderMinute())
            } else {
                // When toggled OFF, cancel any existing alarm
                cancelReminder()
            }
        }

        // 3. Listener for the "Pick Time" button
        pickTimeButton.setOnClickListener {
            showTimePickerDialog()
        }
    }

    private fun showTimePickerDialog() {
        val currentHour = prefs.getReminderHour()
        val currentMinute = prefs.getReminderMinute()

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                // This block is called when the user confirms a new time
                prefs.setReminderTime(hourOfDay, minute)
                updateReminderTimeText() // Update the TextView

                // If reminders are already enabled, reschedule with the new time
                if (remindersSwitch.isChecked) {
                    scheduleReminder(hourOfDay, minute)
                }
            },
            currentHour,
            currentMinute,
            false // Use false for 12-hour format with AM/PM
        )
        timePickerDialog.show()
    }

    private fun updateReminderTimeText() {
        val hour = prefs.getReminderHour()
        val minute = prefs.getReminderMinute()
        // Format the time to display correctly (e.g., 09:05 AM)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        val amPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
        var displayHour = calendar.get(Calendar.HOUR)
        if (displayHour == 0) displayHour = 12 // Handle midnight case

        reminderTimeText.text = String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm)
    }

    // The scheduleReminder and cancelReminder functions below are correct and do not need changes.
    // ...
    private fun scheduleReminder(hour: Int, minute: Int) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${requireContext().packageName}")
                }
                Toast.makeText(requireContext(), "This feature requires a special permission.", Toast.LENGTH_LONG).show()
                startActivity(intent)
                remindersSwitch.isChecked = false
                return
            }
        }

        val intent = Intent(requireContext(), ReminderReceiver::class.java).apply {
            putExtra("message", "Remember to log your workout today")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            2001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        Toast.makeText(requireContext(), "Daily reminder set for ${String.format("%02d:%02d", hour, minute)}", Toast.LENGTH_SHORT).show()
    }

    private fun cancelReminder() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            2001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Toast.makeText(requireContext(), "Daily reminders cancelled.", Toast.LENGTH_SHORT).show()
    }
}
