package com.example.couturecorner.authentication.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.couturecorner.data.local.SharedPreference
import com.example.couturecorner.data.repository.Repo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.graphql.GetCustomerByIdQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
    private val _registrationStatus = MutableLiveData<Boolean>()
    val registrationStatus: LiveData<Boolean> get() = _registrationStatus
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
                repo.saveDraftOrderTag(customerId,"C$customerId")
                Log.i("CartTag", "getCustomerDataTwo: "+repo.getDraftOrderTag(userId = customerId))
            } else {
                Log.e("LoginViewModel", "No Shopify User ID found for email: $email")
            }
        }
    }
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
                    getCustomerData(customerId)
                }
            }
        }
        Log.i("get data customer ", "getCustomerDataTwo: "+"user is null")
    }
    fun registerUserWithGoogle(email: String, password: String?, firstName: String, lastName: String, phoneNumber: String?, idToken: String) {
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential).await()

                val shopifyUserId = repo.registerUser(email, password, firstName, lastName, phoneNumber, idToken)

                if (shopifyUserId != null) {
                    sharedPreference.saveShopifyUserId(email, shopifyUserId)
                    _registrationStatus.postValue(true)
                } else {
                    _registrationStatus.postValue(false)
                }
            } catch (e: Exception) {
                Log.e("UserRegistration", "Error during Google registration: ${e.message}")
                _registrationStatus.postValue(false)
            }
        }
    }
}
