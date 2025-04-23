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
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.firstproject.prog7313_budgetbuddy.viewmodels.ViewModels
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
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
// Activity responsible for user registration functionality
class RegisterActivity : AppCompatActivity() {

    // Firebase authentication instance
    private lateinit var auth: FirebaseAuth

    // ViewModel instance (likely for shared app state or data operations)
    private lateinit var viewModel: ViewModels

    // UI components
    private lateinit var etFullname: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[ViewModels::class.java]

        // Initialize UI components using view IDs
        etFullname = findViewById(R.id.edFullName)
        etEmail = findViewById(R.id.edEmail)
        etPassword = findViewById(R.id.edPassword)
        etConfirmPassword = findViewById(R.id.edPasswordConfirmed)
        btnRegister = findViewById(R.id.btnRegisterPage)
        btnLogin = findViewById(R.id.btnLoginPage)
        btnBack = findViewById(R.id.btnBack)

        // Register button click triggers user registration
        btnRegister.setOnClickListener {
            registerUser()
        }

        // Login button click navigates to login screen
        btnLogin.setOnClickListener {
            navigateToLoginActivity()
        }

        // Back button also navigates to login screen
        btnBack.setOnClickListener {
            navigateToLoginActivity()
        }
    }

    // Navigates to the LoginActivity and finishes the current one
    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Handles user registration logic with validation and Firebase Auth
    private fun registerUser() {
        val fullname = etFullname.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // Validate full name
        if (fullname.isEmpty()) {
            etFullname.error = "Full name is required"
            etFullname.requestFocus()
            return
        }

        // Validate email
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return
        }

        // Validate password
        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            etPassword.requestFocus()
            return
        }

        // Ensure password is at least 6 characters long
        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            etPassword.requestFocus()
            return
        }

        // Validate confirmation password matches
        if (confirmPassword.isEmpty() || confirmPassword != password) {
            etConfirmPassword.error = "Passwords don't match"
            etConfirmPassword.requestFocus()
            return
        }

        // Disable button and show progress
        btnRegister.isEnabled = false
        btnRegister.text = "Registering..."

        // Create user using Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                // Set user display name after successful account creation
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullname)
                    .build()

                authResult.user?.updateProfile(profileUpdates)
                    ?.addOnSuccessListener {
                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                        navigateToLoginActivity()
                    }
                    ?.addOnFailureListener { exception ->
                        // Handle profile update failure
                        Toast.makeText(this, "Failed to update profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                        btnRegister.isEnabled = true
                        btnRegister.text = "Register"
                    }
            }
            .addOnFailureListener { exception ->
                // Handle account creation failure
                Toast.makeText(this, "Registration failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                btnRegister.isEnabled = true
                btnRegister.text = "Register"
            }
    }
}
