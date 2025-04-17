package com.firstproject.prog7313_budgetbuddy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.firstproject.prog7313_budgetbuddy.R
import com.firstproject.prog7313_budgetbuddy.data.entities.User
import com.firstproject.prog7313_budgetbuddy.viewmodels.ViewModels
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userViewModel: ViewModels

    private lateinit var etFullname: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize ViewModel
        userViewModel = ViewModelProvider(this)[ViewModels::class.java]

        // Initialize views
        etFullname = findViewById(R.id.edFullName)
        etEmail = findViewById(R.id.edEmail)
        etPassword = findViewById(R.id.edPassword)
        etConfirmPassword = findViewById(R.id.edPasswordConfirmed)
        btnRegister = findViewById(R.id.btnRegisterPage)
        tvLogin = findViewById(R.id.btnLoginPage)

        // Set click listeners
        btnRegister.setOnClickListener {
            registerUser()
        }

        tvLogin.setOnClickListener {
            // Navigate back to LoginActivity
            finish()
        }
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
        userViewModel.registerWithFirebase(email, password,
            onSuccess = {

                // Create user in local database
                lifecycleScope.launch {
                    val user = User(
                        fullname = fullname,
                        email = email,
                        password = "" // Don't store actual password
                    )
                    userViewModel.createUser(fullname, email, "")
                    auth.createUserWithEmailAndPassword(email, password)


                }
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                // (Optional) Save user data to Firestore here


                startActivity(Intent(this, LoginActivity::class.java))

            },
            onFailure = { message ->
                // Registration failed
                Toast.makeText(this, "Registration failed: $message", Toast.LENGTH_SHORT).show()
                btnRegister.isEnabled = true
                btnRegister.text = "Register"
            }
        )
    }


}

