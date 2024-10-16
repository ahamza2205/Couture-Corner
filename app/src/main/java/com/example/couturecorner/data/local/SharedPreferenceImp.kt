package com.example.couturecorner.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SharedPreferenceImp @Inject constructor(@ApplicationContext private val context: Context): SharedPreference {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

    override fun saveUserLoggedIn(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean("isLoggedIn", isLoggedIn).apply()
    }

    override fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    override fun logoutUser() {
        sharedPreferences.edit().remove("isLoggedIn").apply()
    }

    // ---------------------- Shopify user ID -----------------------------
    override fun saveShopifyUserId(email: String, userId: String) {
        sharedPreferences.edit().putString(email, userId).apply()
    }

    override fun getShopifyUserId(email: String): String? {
        return sharedPreferences.getString(email, null)
    }

    // ---------------------- Address state -----------------------------
    override fun saveAddressState(haveAddress: Boolean) {
        sharedPreferences.edit().putBoolean("haveAddress", haveAddress).apply()
    }

    override fun getAddressState(): Boolean {
        return sharedPreferences.getBoolean("haveAddress", false)
    }

    // ---------------------- Draft order tag -----------------------------
    override fun saveDraftOrderTag(userId: String, tag: String) {
        sharedPreferences.edit().putString("${userId}_tag", tag).apply()
    }

    override fun getDraftOrderTag(userId: String): String? {
        return sharedPreferences.getString("${userId}_tag", null)
    }

    // ---------------------- Draft order ID -----------------------------
    override fun saveDraftOrderId(userId: String, draftOrderId: String) {
        sharedPreferences.edit().putString("${userId}_draftOrderId", draftOrderId).apply()
    }

    override fun getDraftOrderId(userId: String): String? {
        return sharedPreferences.getString("${userId}_draftOrderId", null)
    }

    override fun deleteDraftOrderId(userId: String) {
        sharedPreferences.edit().remove("${userId}_draftOrderId").apply()
    }

    // ---------------------- Selected currency -----------------------------
    override fun saveSelectedCurrency(currency: String) {
        sharedPreferences.edit().putString("selected_currency", currency).apply()
    }

    override fun getSelectedCurrency(): String? {
        return sharedPreferences.getString("selected_currency", "EGP")
    }
}

