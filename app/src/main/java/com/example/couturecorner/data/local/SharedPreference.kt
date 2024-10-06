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
    // Shopify User ID
    fun saveShopifyUserId(shopifyUserId: String) {
        sharedPreferences.edit().putString("shopifyUserId", shopifyUserId).apply()
    }
    fun getShopifyUserId(): String? {
        return sharedPreferences.getString("shopifyUserId", null)
    }
    fun saveAddressState(haveAddress: Boolean){
        sharedPreferences.edit().putBoolean("haveAddress", haveAddress).apply()
    }
    fun getAddressState(): Boolean {
        return sharedPreferences.getBoolean("haveAddress", false)
    }

}