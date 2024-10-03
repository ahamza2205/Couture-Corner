package com.example.couturecorner.authentication.viewmodel

import androidx.lifecycle.ViewModel
import com.example.couturecorner.data.repository.Repo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class SignUpViewModel @Inject constructor(private val repo: Repo) : ViewModel() {

    fun saveUserLoggedIn(isLoggedIn: Boolean) {
        repo.saveUserLoggedIn(isLoggedIn)
    }
}
