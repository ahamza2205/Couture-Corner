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
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _loginStatus = MutableLiveData<Boolean>()
    val loginStatus: LiveData<Boolean> get() = _loginStatus
    fun saveUserLoggedIn(isLoggedIn: Boolean) {
        repo.saveUserLoggedIn(isLoggedIn)
    }

    fun isUserLoggedIn(): Boolean {
        return repo.isUserLoggedIn()
    }
    fun haveAddress() {
        repo.saveAddressState(true)
    }



    fun loginAsGuest() {
        auth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Save guest information to SharedPreferences
                val guestEmail = "guest@example.com"
                sharedPreference.saveUserLoggedIn(true)
                _loginStatus.postValue(true)
            } else {
                _loginStatus.postValue(false)
            }
        }
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




    //
    // Function to update the customer with the not thing take
    private val user = FirebaseAuth.getInstance().currentUser

    fun getCustomerDataTwo() {
        if (user != null) {
            val userEmail = user.email
            Log.i("AddAddress", "updateCustomer: "+userEmail)

            if (userEmail != null) {
                // Get the Shopify customer ID using the email
                val customerId = repo.getShopifyUserId(userEmail)

                if (customerId != null) {
                    repo.saveDraftOrderTag(customerId,"$customerId")
                    Log.i("CartTag", "getCustomerDataTwo: "+repo.getDraftOrderTag(userId = customerId))
                    getCustomerData(customerId)



                }
            }
        }
        Log.i("get data customer ", "getCustomerDataTwo: "+"user is null")
    }


}
