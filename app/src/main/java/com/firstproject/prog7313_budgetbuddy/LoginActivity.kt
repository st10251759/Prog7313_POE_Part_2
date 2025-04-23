package com.firstproject.prog7313_budgetbuddy
/*
 --------------------------------Project Details----------------------------------
 STUDENT NUMBERS: ST10251759   | ST10252746      | ST10266994
 STUDENT NAMES: Cameron Chetty | Theshara Narain | Alyssia Sookdeo
 COURSE: BCAD Year 3
 MODULE: Programming 3C
 MODULE CODE: PROG7313
 ASSESSMENT: Portfolio of Evidence (POE) Part 2
 Github REPO LINK: https://github.com/st10251759/Prog7313_POE_Part_2
 --------------------------------Project Details----------------------------------
*/

/*
 --------------------------------Code Attribution----------------------------------
 Title: Basic syntax | Kotlin Documentation
 Author: Kotlin
 Date Published: 06 November 2024
 Date Accessed: 17 April 2025
 Code Version: v21.20
 Availability: https://kotlinlang.org/docs/basic-syntax.html
  --------------------------------Code Attribution----------------------------------
*/

// Import necessary Android, Firebase and Kotlin libraries
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.firstproject.prog7313_budgetbuddy.RegisterActivity
import com.firstproject.prog7313_budgetbuddy.MainActivity
import com.firstproject.prog7313_budgetbuddy.R
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/*
 --------------------------------Code Attribution----------------------------------
 Title: Get Started with Firebase Authentication on Android  |  Firebase
 Author: Firebase
 Date Published: 2019
 Date Accessed: 16 April 2025
 Code Version: N/A
 Availability: https://firebase.google.com/docs/auth/android/start
  --------------------------------Code Attribution----------------------------------
*/

// Activity for handling user login functionality
class LoginActivity : AppCompatActivity() {

    // Declare UI elements and FirebaseAuth instance
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegisterPage: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enables fullscreen experience with edge-to-edge content
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Applies padding to prevent content from being overlapped by system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Authentication instance
        auth = FirebaseAuth.getInstance()

        // Initialize UI components
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegisterPage = findViewById(R.id.btnRegisterPage)

        // Set up listener for login button
        btnLogin.setOnClickListener {
            // Retrieve user inputs
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validate inputs
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Attempt sign in with Firebase Authentication
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Login successful, navigate to main activity
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish() // Close the login screen
                    } else {
                        // Login failed, show error message
                        Toast.makeText(
                            this,
                            "Login failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        // Set up listener for "Register Now" text
        btnRegisterPage.setOnClickListener {
            // Navigate to the registration screen
            startActivity(Intent(this, RegisterActivity::class.java))
            finish() // Close the login screen
        }
    }
}



