package com.example.fitpath

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.fitpath.utils.SeedDataHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Calendar
import android.widget.*

// --- Use your constructor ---
class SettingsFragment : Fragment() {

    // --- YOUR Firebase variables ---
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText

    // --- THEIR SharedPreferences variable ---
    private val prefs by lazy { Prefs(requireContext()) } // Use your 'Prefs' class

    // --- THEIR Reminder variables ---
    private lateinit var switchReminders: Switch
    private lateinit var reminderTime: TextView
    private lateinit var btnPickTime: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- INITIALIZE YOUR VIEWS AND FIREBASE ---
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        etUsername = view.findViewById(R.id.et_settings_username)
        etEmail = view.findViewById(R.id.et_settings_email)

        // --- MERGED: Theme Switch Logic (Uses your 'prefs') ---
        val sw = view.findViewById<Switch>(R.id.switchTheme)
        sw.isChecked = prefs.darkMode
        sw.setOnCheckedChangeListener { _, isChecked ->
            prefs.darkMode = isChecked
            AppCompatDelegate.setDefaultNightMode(if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
            // No need to recreate(), MainActivity handles this on start
        }

        // --- MERGED: Seeder Button (Your correct version) ---
        view.findViewById<Button>(R.id.btnSeedData)?.setOnClickListener {
            it.isEnabled = false
            Toast.makeText(requireContext(), "Seeding... please wait", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch {
                try {
                    val seeder = SeedDataHelper()
                    val exerciseResult = seeder.seedExercises()
                    if (exerciseResult.isSuccess) {
                        val workoutResult = seeder.seedWorkouts()
                        if (workoutResult.isSuccess) {
                            Toast.makeText(requireContext(), "✓ Successfully added sample exercises AND workouts!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(requireContext(), "Error seeding workouts: ${workoutResult.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Error seeding exercises: ${exerciseResult.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                    }
                    it.isEnabled = true
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    it.isEnabled = true
                }
            }
        }

        // --- MERGED: Reminder Logic (Their code, slightly adapted) ---
        switchReminders = view.findViewById(R.id.switchReminders)
        reminderTime = view.findViewById(R.id.tvReminderTime)
        btnPickTime = view.findViewById(R.id.btnPickTime)

        // Use your 'Prefs' class for consistency
        val remindersEnabled = prefs.getRemindersEnabled() // Assuming you add this to Prefs
        val hour = prefs.getReminderHour() // Assuming you add this
        val minute = prefs.getReminderMinute() // Assuming you add this

        switchReminders.isChecked = remindersEnabled
        reminderTime.text = String.format("%02d:%02d", hour, minute)

        btnPickTime.setOnClickListener {
            val cal = Calendar.getInstance()
            val tp = TimePickerDialog(requireContext(), { _, h, m ->
                prefs.setReminderTime(h, m) // Assuming you add this to Prefs
                reminderTime.text = String.format("%02d:%02d", h, m)
                if (switchReminders.isChecked) scheduleReminder(h, m)
            }, hour, minute, true)
            tp.show()
        }

        switchReminders.setOnCheckedChangeListener { _, isEnabled ->
            if (isEnabled && Build.VERSION.SDK_INT >= 33) { // Check for Notification Permission
                val granted = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                if (!granted) {
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 991)
                }
            }
            prefs.setRemindersEnabled(isEnabled) // Assuming you add this
            val h = prefs.getReminderHour()
            val m = prefs.getReminderMinute()
            if (isEnabled) scheduleReminder(h, m) else cancelReminder()
        }
    }

    // --- MERGED: Your Profile Loader ---
    override fun onStart() {
        super.onStart()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("username")
                        val email = document.getString("email")
                        etUsername.setText(username)
                        etEmail.setText(email)
                    } else {
                        Log.w("SettingsFragment", "No user document found")
                        etUsername.setText("Error: User not found")
                        etEmail.setText(currentUser.email) // Fallback to auth email
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("SettingsFragment", "Error fetching user data: ", exception)
                    etUsername.setText("Error loading data")
                    etEmail.setText(currentUser.email) // Fallback
                }
        } else {
            etUsername.setText("Not Logged In")
            etEmail.setText("Not Logged In")
        }
    }

    // --- MERGED: Their Reminder Functions ---
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