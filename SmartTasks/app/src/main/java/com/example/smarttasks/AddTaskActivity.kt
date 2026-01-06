package com.example.smarttasks

import android.app.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AddTaskActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var tvSelectedDateTime: TextView
    private var selectedCalendar: Calendar? = null
    private val CHANNEL_ID = "task_channel"
    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        // Enable ActionBar back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Task"

        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        tvSelectedDateTime = findViewById(R.id.tvSelectedDateTime)

        val btnPickDate = findViewById<Button>(R.id.btnPickDate)
        val btnPickTime = findViewById<Button>(R.id.btnPickTime)
        val btnSave = findViewById<Button>(R.id.btnSaveTask)

        btnPickDate.setOnClickListener { pickDate() }
        btnPickTime.setOnClickListener { pickTime() }
        btnSave.setOnClickListener { saveTask() }

        createNotificationChannel()
        checkNotificationPermission()
    }

    private fun pickDate() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                if (selectedCalendar == null) selectedCalendar = Calendar.getInstance()
                selectedCalendar!!.set(year, month, day)
                updateDateTimeLabel()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun pickTime() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute ->
                if (selectedCalendar == null) selectedCalendar = Calendar.getInstance()
                selectedCalendar!!.set(Calendar.HOUR_OF_DAY, hour)
                selectedCalendar!!.set(Calendar.MINUTE, minute)
                selectedCalendar!!.set(Calendar.SECOND, 0)

                val now = Calendar.getInstance()
                if (selectedCalendar!!.timeInMillis < now.timeInMillis) {
                    Toast.makeText(this, "Cannot select past time", Toast.LENGTH_SHORT).show()
                    selectedCalendar = now
                }

                updateDateTimeLabel()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updateDateTimeLabel() {
        selectedCalendar?.let {
            val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
            tvSelectedDateTime.text = "Selected: ${sdf.format(it.time)}"
        }
    }

    private fun saveTask() {
        val title = etTitle.text.toString().trim()
        val desc = etDescription.text.toString().trim()
        val time = selectedCalendar?.timeInMillis

        if (title.isEmpty() || desc.isEmpty() || time == null) {
            Toast.makeText(this, "Please fill all fields & select date/time", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("TaskMatePrefs", MODE_PRIVATE)
        val userId = prefs.getString("user_id", null)
        val userEmail = prefs.getString("user_email", null)

        if (userId == null || userEmail == null) {
            Toast.makeText(this, "You must be logged in to save tasks", Toast.LENGTH_SHORT).show()
            return
        }

        // Save task to Firebase
        val dbRef = FirebaseDatabase.getInstance().getReference("tasks")
        val taskId = dbRef.push().key!!
        val task = mapOf(
            "id" to taskId,
            "title" to title,
            "description" to desc,
            "dueTime" to time,
            "userId" to userId
        )

        dbRef.child(taskId).setValue(task)
            .addOnSuccessListener {
                Toast.makeText(this, "Task saved & reminder set!", Toast.LENGTH_LONG).show()
                scheduleEmailReminder(title, desc, time, userEmail)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save task: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
    }

    private fun scheduleEmailReminder(title: String, desc: String, dueTime: Long, email: String) {
        val delay = dueTime - System.currentTimeMillis()
        if (delay <= 0) return

        val data = workDataOf(
            "title" to title,
            "desc" to desc,
            "email" to email,
            "dueTime" to dueTime
        )

        val workRequest = OneTimeWorkRequestBuilder<EmailWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val sound: Uri = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for task reminders"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                setSound(sound, null)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    // **Back button handling**
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish() // go back
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
