package com.firstproject.prog7313_budgetbuddy

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

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: ViewModels

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

        // Initialize views
        etFullname = findViewById(R.id.edFullName)
        etEmail = findViewById(R.id.edEmail)
        etPassword = findViewById(R.id.edPassword)
        etConfirmPassword = findViewById(R.id.edPasswordConfirmed)
        btnRegister = findViewById(R.id.btnRegisterPage)
        btnLogin = findViewById(R.id.btnLoginPage)
        btnBack = findViewById(R.id.btnBack)

        // Set click listeners
        btnRegister.setOnClickListener {
            registerUser()
        }

        // Navigate to LoginActivity when login button is clicked
        btnLogin.setOnClickListener {
            navigateToLoginActivity()
        }

        // Also navigate to LoginActivity when back button is clicked
        btnBack.setOnClickListener {
            navigateToLoginActivity()
        }
    }

    private fun navigateToLoginActivity() {
        // Create explicit intent to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Close the current activity
    }

    private fun registerUser() {
        val fullname = etFullname.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // Validate fields
        if (fullname.isEmpty()) {
            etFullname.error = "Full name is required"
            etFullname.requestFocus()
            return
        }

        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            etPassword.requestFocus()
            return
        }

        if (confirmPassword.isEmpty() || confirmPassword != password) {
            etConfirmPassword.error = "Passwords don't match"
            etConfirmPassword.requestFocus()
            return
        }

        // Show loading
        btnRegister.isEnabled = false
        btnRegister.text = "Registering..."

        // Register with Firebase
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                // Set display name in Firebase
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullname)
                    .build()

                authResult.user?.updateProfile(profileUpdates)
                    ?.addOnSuccessListener {
                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                        navigateToLoginActivity()
                    }
                    ?.addOnFailureListener { exception ->
                        Toast.makeText(this, "Failed to update profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                        btnRegister.isEnabled = true
                        btnRegister.text = "Register"
                    }
            }
            .addOnFailureListener { exception ->
                // Registration failed
                Toast.makeText(this, "Registration failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                btnRegister.isEnabled = true
                btnRegister.text = "Register"
            }
    }
}