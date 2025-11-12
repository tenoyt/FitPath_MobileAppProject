package com.example.fitpath

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import java.util.Calendar
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val session by lazy { SessionManager(requireContext()) }
    private val prefs by lazy { requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val switchTheme: Switch = view.findViewById(R.id.switchTheme)
        val switchReminders: Switch = view.findViewById(R.id.switchReminders)
        val reminderTime: TextView = view.findViewById(R.id.tvReminderTime)
        val btnPickTime: Button = view.findViewById(R.id.btnPickTime)
        val etName: EditText = view.findViewById(R.id.etName)
        val etEmail: EditText = view.findViewById(R.id.etEmail)
        val btnSaveProfile: Button = view.findViewById(R.id.btnSaveProfile)
        val etNewPassword: EditText = view.findViewById(R.id.etNewPassword)
        val btnChangePassword: Button = view.findViewById(R.id.btnChangePassword)
        val switchPrivacy: Switch = view.findViewById(R.id.switchPrivacy)

        // Load existing values
        val dark = requireContext().getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE).getBoolean("dark_mode", false)
        switchTheme.isChecked = dark

        etName.setText(prefs.getString("name", ""))
        etEmail.setText(prefs.getString("email", ""))
        switchPrivacy.isChecked = prefs.getBoolean("share_data", false)
        switchReminders.isChecked = prefs.getBoolean("reminders_enabled", false)
        val hour = prefs.getInt("reminder_hour", 9)
        val minute = prefs.getInt("reminder_minute", 0)
        reminderTime.text = String.format("%02d:%02d", hour, minute)

        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            Prefs(requireContext()).darkMode = isChecked
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            requireActivity().recreate()
        }


        btnSaveProfile.setOnClickListener {
            prefs.edit()
                .putString("name", etName.text.toString())
                .putString("email", etEmail.text.toString())
                .apply()
            Toast.makeText(requireContext(), "Profile saved", Toast.LENGTH_SHORT).show()
        }

        btnChangePassword.setOnClickListener {
            val newPass = etNewPassword.text.toString()
            if (newPass.length < 6) {
                Toast.makeText(requireContext(), "Password too short", Toast.LENGTH_SHORT).show()
            } else {
                prefs.edit().putString("password", newPass).apply()
                Toast.makeText(requireContext(), "Password updated", Toast.LENGTH_SHORT).show()
                etNewPassword.setText("")
            }
        }

        switchPrivacy.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("share_data", checked).apply()
        }

        btnPickTime.setOnClickListener {
            val cal = Calendar.getInstance()
            val tp = TimePickerDialog(requireContext(), { _, h, m ->
                prefs.edit().putInt("reminder_hour", h).putInt("reminder_minute", m).apply()
                reminderTime.text = String.format("%02d:%02d", h, m)
                if (switchReminders.isChecked) scheduleReminder(h, m)
            }, hour, minute, true)
            tp.show()
        }

        
switchReminders.setOnCheckedChangeListener { _, isEnabled ->
                if (isEnabled && Build.VERSION.SDK_INT >= 33) {
                    val granted = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                    if (!granted) {
                        ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 991)
                    }
                }

            prefs.edit().putBoolean("reminders_enabled", isEnabled).apply()
            val h = prefs.getInt("reminder_hour", 9)
            val m = prefs.getInt("reminder_minute", 0)
            if (isEnabled) scheduleReminder(h, m) else cancelReminder()
        }
    }

    private fun scheduleReminder(hour: Int, minute: Int) {
        val am = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), ReminderReceiver::class.java).apply {
            putExtra("message", "Remember to log your workout today")
        }
        val pi = PendingIntent.getBroadcast(requireContext(), 2001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        am.setRepeating(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pi
        )
        Toast.makeText(requireContext(), "Daily reminder set", Toast.LENGTH_SHORT).show()
    }

    private fun cancelReminder() {
        val am = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), ReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(requireContext(), 2001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        am.cancel(pi)
        Toast.makeText(requireContext(), "Reminders disabled", Toast.LENGTH_SHORT).show()
    }
}