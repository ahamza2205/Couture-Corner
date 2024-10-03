package com.example.couturecorner.data.local

import android.content.Context
import android.content.SharedPreferences

class SharedPreference(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

    fun saveUserLoggedIn(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean("isLoggedIn", isLoggedIn).apply()
    }

    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    fun logoutUser() {
        sharedPreferences.edit().remove("isLoggedIn").apply()
    }
}