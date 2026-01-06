package com.example.smarttasks

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ViewTasksActivity : AppCompatActivity() {

    private lateinit var tasksContainer: LinearLayout
    private val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_tasks)

        // Enable Up button in the ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Tasks"

        tasksContainer = findViewById(R.id.tasksContainer)

        val prefs = getSharedPreferences("TaskMatePrefs", MODE_PRIVATE)
        val userId = prefs.getString("user_id", null)

        if (userId != null) {
            loadTasks(userId)
        } else {
            showMessage("You must be logged in to view tasks.")
        }
    }

    private fun loadTasks(userId: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("tasks")
        dbRef.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    tasksContainer.removeAllViews()
                    if (!snapshot.exists()) {
                        showMessage("No tasks found.")
                        return
                    }

                    val now = System.currentTimeMillis()
                    for (snap in snapshot.children) {
                        val title = snap.child("title").getValue(String::class.java) ?: "No title"
                        val desc = snap.child("description").getValue(String::class.java) ?: ""
                        val dueTime = snap.child("dueTime").getValue(Long::class.java)
                        val dueFormatted = dueTime?.let { sdf.format(Date(it)) } ?: "N/A"

                        val isPast = dueTime != null && dueTime < now
                        addTaskCard(title, desc, dueFormatted, isPast)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showMessage("Failed to load tasks: ${error.message}")
                }
            })
    }

    private fun showMessage(message: String) {
        tasksContainer.removeAllViews()
        val tv = TextView(this)
        tv.text = message
        tv.textSize = 16f
        tv.setTextColor(Color.DKGRAY)
        tv.gravity = Gravity.CENTER
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 20, 0, 0)
        tv.layoutParams = params
        tasksContainer.addView(tv)
    }

    private fun addTaskCard(title: String, description: String, due: String, isPast: Boolean) {
        val card = CardView(this).apply {
            radius = 16f
            setCardBackgroundColor(if (isPast) Color.parseColor("#FFEBEE") else Color.WHITE)
            cardElevation = 8f
            useCompatPadding = true
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 12, 0, 12)
            layoutParams = params
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val tvTitle = TextView(this).apply {
            text = title
            textSize = 18f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 8)
        }

        val tvDesc = TextView(this).apply {
            text = description
            textSize = 16f
            setTextColor(Color.DKGRAY)
            setPadding(0, 0, 0, 8)
        }

        val tvDue = TextView(this).apply {
            text = if (isPast) "Past Event: $due" else "Due: $due"
            textSize = 14f
            setTextColor(if (isPast) Color.RED else Color.GRAY)
        }

        layout.addView(tvTitle)
        layout.addView(tvDesc)
        layout.addView(tvDue)

        card.addView(layout)
        tasksContainer.addView(card)
    }

    // Handle ActionBar back button
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish() // simply close this activity and go back
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
