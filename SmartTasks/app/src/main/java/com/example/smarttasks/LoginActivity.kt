package com.example.smarttasks

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var tvSignupLink: TextView
    private lateinit var btnBackHome: Button

    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        email = findViewById(R.id.Login_Email)
        password = findViewById(R.id.Login_Password)
        loginButton = findViewById(R.id.btnLogin)
        tvSignupLink = findViewById(R.id.tvSignupLink)
        btnBackHome = findViewById(R.id.btnBackHome)

        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        loginButton.setOnClickListener {
            val userEmail = email.text.toString().trim()
            val userPassword = password.text.toString().trim()

            if (userEmail.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            databaseReference.orderByChild("email").equalTo(userEmail)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            var success = false
                            for (userSnap in snapshot.children) {
                                val user = userSnap.getValue(FirebaseData::class.java)
                                if (user != null && user.password == userPassword) {
                                    // Save logged-in user info locally
                                    val prefs = getSharedPreferences("TaskMatePrefs", MODE_PRIVATE)
                                    prefs.edit()
                                        .putString("user_id", user.userId)
                                        .putString("user_email", user.email)
                                        .putString("user_name", user.name)
                                        .apply()

                                    Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                                    finish()
                                    success = true
                                    break
                                }
                            }
                            if (!success) {
                                Toast.makeText(this@LoginActivity, "Invalid password", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "User not found", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@LoginActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        tvSignupLink.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }

        btnBackHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            })
            finish()
        }
    }
}
