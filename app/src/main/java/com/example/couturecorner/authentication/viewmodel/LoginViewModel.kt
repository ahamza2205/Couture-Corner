package com.example.couturecorner.authentication.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.couturecorner.data.local.SharedPreference
import com.example.couturecorner.data.repository.Repo
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

    fun haveAddress() {
        repo.saveAddressState(true)
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
                    Log.e("LoginViewModel", "Error fetching customer data: ${e.message}")
                }
            } else {
                Log.e("LoginViewModel", "Customer ID is null")
                _customerData.postValue(null)
            }
        }
    }

}
