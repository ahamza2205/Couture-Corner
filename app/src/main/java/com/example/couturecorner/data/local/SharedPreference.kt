package com.example.couturecorner.data.local

interface SharedPreference {
    fun saveUserLoggedIn(isLoggedIn: Boolean)
    fun isUserLoggedIn(): Boolean
    fun logoutUser()
    fun saveShopifyUserId(email: String, userId: String)
    fun getShopifyUserId(email: String): String?
    fun saveAddressState(haveAddress: Boolean)
    fun getAddressState(): Boolean
    fun saveDraftOrderTag(userId: String, tag: String)
    fun getDraftOrderTag(userId: String): String?
    fun saveDraftOrderId(userId: String, draftOrderId: String)
    fun getDraftOrderId(userId: String): String?
    fun deleteDraftOrderId(userId: String)
    fun saveSelectedCurrency(currency: String)
    fun getSelectedCurrency(): String?
}