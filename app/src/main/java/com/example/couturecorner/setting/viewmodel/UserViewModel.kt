package com.example.couturecorner.setting.viewmodel
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.couturecorner.data.repository.Repo
import com.google.firebase.auth.FirebaseAuth
import com.graphql.GetCustomerByIdQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel  @Inject constructor(
    private val repo: Repo,

) : ViewModel() {
    private val _userData = MutableLiveData<GetCustomerByIdQuery.Customer?>()
    val userData: LiveData<GetCustomerByIdQuery.Customer?> get() = _userData

    private val user = FirebaseAuth.getInstance().currentUser


    init {
        getCustomerData()
    }
    fun getCustomerDataFromApi(customerId: String) {
        viewModelScope.launch {
            try {
                val customer = repo.getCustomerById(customerId)
                _userData.postValue(customer)
            } catch (e: Exception) {
                _userData.postValue(null)
                Log.e("UserViewModel", "Error fetching customer data: ${e.message}")
            }
        }
    }

    fun getCustomerData() {
        if (user != null) {
            val userEmail = user.email
            Log.i("UserViewModel", "updateCustomer: $userEmail")
            if (userEmail != null) {
                // Get the Shopify customer ID using the email
                val customerId = repo.getShopifyUserId(userEmail)
                Log.i("UserViewModel", "getCustomerDataTwo: $customerId")
                if (customerId != null) {
                    getCustomerDataFromApi(customerId)
                }
            }
        } else {
            Log.i("UserViewModel", "getCustomerDataTwo: user is null")
        }
    }
}