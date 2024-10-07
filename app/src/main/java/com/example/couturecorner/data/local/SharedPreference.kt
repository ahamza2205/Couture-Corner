package com.example.couturecorner.data.local

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

class SharedPreference @Inject constructor(context: Context) {

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


    // ---------------------- shopify user id -----------------------------
    fun saveShopifyUserId(email: String, userId: String) {
        sharedPreferences.edit().putString(email, userId).apply()
    }

    fun getShopifyUserId(email: String): String? {
        return sharedPreferences.getString(email, null)
    }
    // ---------------------- Address state  -----------------------------
    // ---------------------- Address state  ------------------------------


    fun saveAddressState(haveAddress: Boolean){
        sharedPreferences.edit().putBoolean("haveAddress", haveAddress).apply()
    }
    fun getAddressState(): Boolean {
        return sharedPreferences.getBoolean("haveAddress", false)
    }
    // ----------------------  Draft Order Tag -----------------------------
    // ----------------------  Draft Order Tag -----------------------------

    fun saveDraftOrderTag(userId: String, tag: String) {
        sharedPreferences.edit().putString(userId, tag).apply()
    }
    fun getDraftOrderTag(userId: String): String? {
        return sharedPreferences.getString(userId, null)
    }


}