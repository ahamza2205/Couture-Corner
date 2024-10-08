package com.example.couturecorner.setting.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.couturecorner.data.repository.Irepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: Irepo
) : ViewModel() {

    fun logoutUser() {
        viewModelScope.launch {
            repo.logoutUser()
        }
    }

    fun isUserLoggedIn(): Boolean {
        return repo.isUserLoggedIn()
    }
}
