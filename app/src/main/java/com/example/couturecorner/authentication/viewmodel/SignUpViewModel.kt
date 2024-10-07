package com.example.couturecorner.authentication.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.couturecorner.data.local.SharedPreference // تأكد من استيراد كلاس SharedPreference
import com.example.couturecorner.data.repository.Repo
import com.graphql.GetCustomerByIdQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repo: Repo,
    private val sharedPreference: SharedPreference
) : ViewModel() {

    private val _registrationStatus = MutableLiveData<Boolean>()
    val registrationStatus: LiveData<Boolean> get() = _registrationStatus

    private val _customerData = MutableLiveData<GetCustomerByIdQuery.Customer?>()
    val customerData: LiveData<GetCustomerByIdQuery.Customer?> get() = _customerData

    // Save login status in shared preferences
    fun saveUserLoggedIn(isLoggedIn: Boolean) {
        repo.saveUserLoggedIn(isLoggedIn)
    }

    // Shopify registration method
    fun registerUser(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNumber: String
    ) {
        viewModelScope.launch {
            try {
                // Call repo to register user in Firebase and Shopify
                val shopifyUserId = repo.registerUser(email, password, firstName, lastName, phoneNumber)

                if (shopifyUserId != null) {
                    // Save Shopify User ID to shared preferences
                    sharedPreference.saveShopifyUserId(email, shopifyUserId)
                    _registrationStatus.postValue(true)
                    Log.d("SignUpViewModel", "Shopify user created successfully: $shopifyUserId")
                } else {
                    _registrationStatus.postValue(false)
                    Log.e("SignUpViewModel", "Failed to create Shopify user: User ID is null")
                }
            } catch (e: Exception) {
                _registrationStatus.postValue(false)
                Log.e("SignUpViewModel", "Error creating Shopify user: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // Method to fetch customer data by ID
/*    fun getCustomerData(customerId: String) {
        viewModelScope.launch {
            try {
                val customer = repo.getCustomerById(customerId)
                _customerData.postValue(customer)
            } catch (e: Exception) {
                _customerData.postValue(null)
                Log.e("SignUpViewModel", "Error fetching customer data: ${e.message}")
            }
        }
    }*/
}

