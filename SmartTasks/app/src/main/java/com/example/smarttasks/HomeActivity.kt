package com.example.smarttasks

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set dynamic welcome message
        val tvWelcomeUser = findViewById<TextView>(R.id.tvWelcomeUser)
        val prefs = getSharedPreferences("TaskMatePrefs", MODE_PRIVATE)
        val username = prefs.getString("user_name", "User") // default to "User"
        tvWelcomeUser.text = "Welcome back, $username 👋"

        // Logout card
        val logoutCard = findViewById<CardView>(R.id.logoutCard)
        logoutCard.setOnClickListener { showLogoutConfirmation() }

        // Add Task card
        val addTaskCard = findViewById<CardView>(R.id.addTaskCard)
        addTaskCard.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }

        // Profile card
        val profileCard = findViewById<CardView>(R.id.profileCard)
        profileCard.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // View Tasks card
        val viewTasksCard = findViewById<CardView>(R.id.TasksCard)
        viewTasksCard.setOnClickListener {
            startActivity(Intent(this, ViewTasksActivity::class.java))
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, _ ->
                performLogout()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun performLogout() {
        val prefs = getSharedPreferences("TaskMatePrefs", MODE_PRIVATE)
        prefs.edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
