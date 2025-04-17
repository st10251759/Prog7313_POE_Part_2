package com.firstproject.prog7313_budgetbuddy.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.firstproject.prog7313_budgetbuddy.BudgetBuddyDatabase

import com.firstproject.prog7313_budgetbuddy.data.entities.*
import com.firstproject.prog7313_budgetbuddy.data.repositories.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.*

class ViewModels(application: Application) : AndroidViewModel(application) {
    private val repository: Repositories
    private val auth = FirebaseAuth.getInstance()

    init {
        val userDao = BudgetBuddyDatabase.getDatabase(application).userDao()
        repository = Repositories(userDao)
    }

    fun createUser(name: String, email: String, password: String) = viewModelScope.launch {
        val user = User(fullname = name, email = email, password = password)
        repository.insertUser(user)
    }

    suspend fun getUserByEmail(email: String): User? {
        return repository.getUserByEmail(email)
    }

    fun loginWithFirebase(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Authentication failed") }
    }

    fun registerWithFirebase(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Registration failed") }
    }

    fun getCurrentUserId(): Int? {
        // You would need to implement a mapping between Firebase user and your local user
        // This is a placeholder
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            // Get the matching user ID from your database
            // This would likely involve a query to your database based on the Firebase user email
            viewModelScope.launch {
                repository.getUserByEmail(firebaseUser.email ?: "")?.userId
            }
            1 // Placeholder, replace with actual logic
        } else {
            null
        }
    }
}



