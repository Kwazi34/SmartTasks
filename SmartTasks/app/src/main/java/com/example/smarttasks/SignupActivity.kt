package com.example.smarttasks

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {

    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var signupButton: Button
    private lateinit var tvLoginLink: TextView

    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup2) // Make sure this XML exists

        // Initialize views
        name = findViewById(R.id.Signup_name)
        email = findViewById(R.id.Signup_Email)
        password = findViewById(R.id.Signup_Password)
        confirmPassword = findViewById(R.id.Signup_ConfirmPassword)
        signupButton = findViewById(R.id.btnSignup)
        tvLoginLink = findViewById(R.id.tvLoginLink)

        // Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        // Signup button click
        signupButton.setOnClickListener {
            val userName = name.text.toString().trim()
            val userEmail = email.text.toString().trim()
            val userPassword = password.text.toString().trim()
            val userConfirmPassword = confirmPassword.text.toString().trim()

            if (!validateInputs(userName, userEmail, userPassword, userConfirmPassword)) return@setOnClickListener

            val userId = databaseReference.push().key
            if (userId != null) {
                val firebaseData = FirebaseData(userName, userEmail, userPassword, userId)
                databaseReference.child(userId).setValue(firebaseData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Signup failed. Try again.", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Login link click
        tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Optional: Edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Validate user input
    private fun validateInputs(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (name.isEmpty() || name.length < 2 || !name.matches(Regex("^[A-Za-z ]+$"))) {
            this.name.error = "Enter a valid name"
            return false
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.email.error = "Enter a valid email"
            return false
        }

        val passwordPattern = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$")
        if (password.isEmpty() || !passwordPattern.matches(password)) {
            this.password.error = "Password must have 8 chars, uppercase, lowercase, and number"
            return false
        }

        if (confirmPassword != password) {
            this.confirmPassword.error = "Passwords do not match"
            return false
        }

        return true
    }
}
