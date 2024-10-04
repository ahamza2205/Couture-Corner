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



    // --------------------------- shared preference ------------------------------------
    fun saveUserLoggedIn(isLoggedIn: Boolean) {
        repo.saveUserLoggedIn(isLoggedIn)
    }

    // --------------------------- shopify registration -------------------------------
    fun registerUser(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNumber: String
    ) {
        viewModelScope.launch {
            try {
                val shopifyUserId = repo.registerUser(email, password, firstName, lastName, phoneNumber)

                if (shopifyUserId != null) {
                    // Save the Shopify User ID in shared preferences
                    sharedPreference.saveShopifyUserId(shopifyUserId)

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
    fun getCustomerData() {
        val customerId = sharedPreference.getShopifyUserId()

        viewModelScope.launch {
            if (customerId != null) {
                try {
                    val customer = repo.getCustomerById(customerId)
                    _customerData.postValue(customer)
                } catch (e: Exception) {
                    _customerData.postValue(null)
                    Log.e("SignUpViewModel", "Error fetching customer data: ${e.message}")
                }
            } else {
                Log.e("SignUpViewModel", "Customer ID is null")
                _customerData.postValue(null)
            }
        }
    }
}
