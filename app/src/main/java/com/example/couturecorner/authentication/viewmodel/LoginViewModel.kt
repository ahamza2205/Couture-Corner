package com.example.couturecorner.authentication.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.couturecorner.data.local.SharedPreference
import com.example.couturecorner.data.repository.Repo
import com.google.firebase.auth.FirebaseAuth
import com.graphql.GetCustomerByIdQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: Repo,
    private val sharedPreference: SharedPreference
) : ViewModel() {
    private val _customerData = MutableLiveData<GetCustomerByIdQuery.Customer?>()
    val customerData: LiveData<GetCustomerByIdQuery.Customer?> get() = _customerData
    fun saveUserLoggedIn(isLoggedIn: Boolean) {
        repo.saveUserLoggedIn(isLoggedIn)
    }

    fun isUserLoggedIn(): Boolean {
        return repo.isUserLoggedIn()
    }

    fun getCustomerData(customerId: String) {
        viewModelScope.launch {
            try {
                val customer = repo.getCustomerById(customerId)
                _customerData.postValue(customer)
            } catch (e: Exception) {
                _customerData.postValue(null)
                Log.e("LoginViewModel", "Error fetching customer data: ${e.message}")
            }
        }
    }

    fun getCustomerDataFromFirebaseAuth(email: String) {
        viewModelScope.launch {
            // Get Shopify user ID from shared preferences
            val customerId = sharedPreference.getShopifyUserId(email)
            if (customerId != null) {
                getCustomerData(customerId)
            } else {
                Log.e("LoginViewModel", "No Shopify User ID found for email: $email")
            }
        }
    }


}
