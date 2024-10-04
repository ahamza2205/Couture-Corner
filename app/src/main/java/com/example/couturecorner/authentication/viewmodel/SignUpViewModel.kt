package com.example.couturecorner.authentication.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.couturecorner.data.repository.Repo
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
@HiltViewModel
class SignUpViewModel @Inject constructor(private val repo: Repo) : ViewModel() {

    private val _registrationStatus = MutableLiveData<Boolean>()
    val registrationStatus: LiveData<Boolean> get() = _registrationStatus

    // --------------------------- shared preference ------------------------------------
    fun saveUserLoggedIn(isLoggedIn: Boolean) {
        repo.saveUserLoggedIn(isLoggedIn)
    }

    // --------------------------- shopify registration -------------------------------
    fun registerUser(email: String, password: String, firstName: String, lastName: String, phoneNumber: String) {
        viewModelScope.launch {
            try {
                val shopifyUserId = repo.registerUser(email, password, firstName, lastName, phoneNumber)
                _registrationStatus.postValue(true)
                Log.d("SignUpViewModel", "Shopify user created successfully: $shopifyUserId")
            } catch (e: Exception) {
                _registrationStatus.postValue(false)
                Log.e("SignUpViewModel", "Error creating Shopify user: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun registerUserWithEmailVerification(email: String, password: String, firstName: String, lastName: String, phoneNumber: String) {
        viewModelScope.launch {
            try {
                val auth = FirebaseAuth.getInstance()
                val firebaseUserId = auth.createUserWithEmailAndPassword(email, password).await()

                if (firebaseUserId.user != null) {
                    Log.d("SignUpViewModel", "Firebase user created successfully: ${firebaseUserId.user!!.email}") // سجل نجاح إنشاء مستخدم Firebase
                    firebaseUserId.user?.sendEmailVerification()?.await()
                    _registrationStatus.postValue(true)

                    val shopifyUserId = repo.registerUser(email, password, firstName, lastName, phoneNumber)
                    Log.d("SignUpViewModel", "Shopify user created successfully: $shopifyUserId")
                } else {
                    Log.e("SignUpViewModel", "Failed to create Firebase user")
                    _registrationStatus.postValue(false)
                }

            } catch (e: Exception) {
                _registrationStatus.postValue(false)
                Log.e("SignUpViewModel", "Error creating user: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
