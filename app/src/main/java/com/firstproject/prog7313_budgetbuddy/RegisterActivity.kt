package com.firstproject.firebasics

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.firstproject.prog7313_budgetbuddy.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var tilFullName: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilPasswordConfirm: TextInputLayout

    private lateinit var edFullName: TextInputEditText
    private lateinit var edEmail: TextInputEditText
    private lateinit var edPassword: TextInputEditText
    private lateinit var edPasswordConfirms: TextInputEditText

    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var btnBack: ImageButton

    private lateinit var passwordStrengthContainer: LinearLayout
    private lateinit var passwordStrengthBar: ProgressBar

    // declare firebase auth as variable
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize TextInputLayouts
        tilFullName = findViewById(R.id.tilFullName)
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        tilPasswordConfirm = findViewById(R.id.tilPasswordConfirm)

        // Initialize EditTexts
        edFullName = findViewById(R.id.edFullName)
        edEmail = findViewById(R.id.edEmail)
        edPassword = findViewById(R.id.edPassword)
        edPasswordConfirms = findViewById(R.id.edPasswordConfirmed)

        // Initialize Buttons
        btnBack = findViewById(R.id.btnBack)
        btnLogin = findViewById(R.id.btnLoginPage)
        btnRegister = findViewById(R.id.btnRegisterPage)

        // Initialize password strength elements
        passwordStrengthContainer = findViewById(R.id.passwordStrengthContainer)
        passwordStrengthBar = findViewById(R.id.passwordStrengthBar)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Set up password strength meter
        edPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s.toString()

                if (password.isEmpty()) {
                    passwordStrengthContainer.visibility = View.GONE
                } else {
                    passwordStrengthContainer.visibility = View.VISIBLE
                    updatePasswordStrengthView(password)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Set up back button action
        btnBack.setOnClickListener {
            onBackPressed()
        }

        // Set up login button action
        btnLogin.setOnClickListener {
            // Navigate to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Close this activity
        }

        // Set up register button action
        btnRegister.setOnClickListener {
            val fullName = edFullName.text.toString().trim()
            val email = edEmail.text.toString().trim()
            val password = edPassword.text.toString().trim()
            val confirmPassword = edPasswordConfirms.text.toString().trim()

            // Input validation
            var isValid = true

            if (fullName.isEmpty()) {
                tilFullName.error = "Full name cannot be empty"
                isValid = false
            } else {
                tilFullName.error = null
            }

            if (email.isEmpty()) {
                tilEmail.error = "Email cannot be empty"
                isValid = false
            } else if (!isValidEmail(email)) {
                tilEmail.error = "Please enter a valid email address"
                isValid = false
            } else {
                tilEmail.error = null
            }

            if (password.isEmpty()) {
                tilPassword.error = "Password cannot be empty"
                isValid = false
            } else if (password.length < 8) {
                tilPassword.error = "Password must be at least 8 characters long"
                isValid = false
            } else {
                tilPassword.error = null
            }

            if (confirmPassword.isEmpty()) {
                tilPasswordConfirm.error = "Please confirm your password"
                isValid = false
            } else if (confirmPassword != password) {
                tilPasswordConfirm.error = "Passwords don't match"
                isValid = false
            } else {
                tilPasswordConfirm.error = null
            }

            if (isValid) {
                // Disable button and show progress (could add a progress indicator here)
                btnRegister.isEnabled = false

                // Firebase Authentication - Register user
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        btnRegister.isEnabled = true

                        if (task.isSuccessful) {
                            // Save additional user data if needed (e.g., full name)
                            // This could be done with Firestore or Realtime Database

                            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()

                            // Redirect to LoginActivity
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish() // Close RegisterActivity
                        } else {
                            Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    private fun updatePasswordStrengthView(password: String) {
        // Calculate password strength
        val strength = when {
            password.length < 6 -> {
                // Weak password
                passwordStrengthBar.progress = 30
                passwordStrengthBar.progressDrawable = ContextCompat.getDrawable(this, R.drawable.password_strength_weak)
            }
            password.length < 10 -> {
                // Medium password
                passwordStrengthBar.progress = 60
                passwordStrengthBar.progressDrawable = ContextCompat.getDrawable(this, R.drawable.password_strength_medium)
            }
            else -> {
                // Strong password
                passwordStrengthBar.progress = 100
                passwordStrengthBar.progressDrawable = ContextCompat.getDrawable(this, R.drawable.password_strength_strong)
            }
        }
    }
}