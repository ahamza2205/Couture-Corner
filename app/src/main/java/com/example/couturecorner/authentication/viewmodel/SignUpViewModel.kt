package com.example.couturecorner.authentication.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.couturecorner.data.repository.Repo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
                repo.registerUser(email, password, firstName, lastName, phoneNumber)
                _registrationStatus.postValue(true)
            } catch (e: Exception) {
                _registrationStatus.postValue(false)
                e.printStackTrace()
            }
        }
    }
}

