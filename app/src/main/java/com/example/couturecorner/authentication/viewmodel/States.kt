package com.example.couturecorner.authentication.viewmodel

import android.annotation.SuppressLint
import com.google.firebase.firestore.auth.User


// Define login and registration states using sealed classes

sealed class LoginState {
    object Loading : LoginState()
    data class Success(@SuppressLint("RestrictedApi") val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}
sealed class RegistrationState {
    object Loading : RegistrationState()
    object Success : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}