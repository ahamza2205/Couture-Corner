package com.example.couturecorner.authentication.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.couturecorner.data.local.SharedPreference // تأكد من استيراد كلاس SharedPreference
import com.example.couturecorner.data.repository.Repo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.graphql.GetCustomerByIdQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
                val shopifyUserId = repo.registerUser(email, password, firstName, lastName, phoneNumber)

                if (shopifyUserId != null) {
                    sharedPreference.saveShopifyUserId(email, shopifyUserId)
                    repo.saveDraftOrderTag(userId = shopifyUserId, tag = "C$shopifyUserId")
                    Log.i("CartTag", "getCustomerDataTwo: "+repo.getDraftOrderTag(userId = shopifyUserId))

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
    fun registerUserWithEmailVerification(email: String, password: String, firstName: String, lastName: String, phoneNumber: String) {
        viewModelScope.launch {
            try {
                val auth = FirebaseAuth.getInstance()
                val firebaseUserId = auth.createUserWithEmailAndPassword(email, password).await()

                if (firebaseUserId.user != null) {
                    // سجل المستخدم في Firebase
                    Log.d("SignUpViewModel", "Firebase user created successfully: ${firebaseUserId.user!!.email}")

                    // أرسل رسالة تحقق عبر البريد الإلكتروني
                    firebaseUserId.user?.sendEmailVerification()?.await()
                    Log.d("SignUpViewModel", "Verification email sent to: ${firebaseUserId.user!!.email}")

                    // سجل المستخدم في النظام الخاص بك
                    val shopifyUserId = repo.registerUser(email, password, firstName, lastName, phoneNumber)
                    Log.d("SignUpViewModel", "Shopify user created successfully: $shopifyUserId")

                    // انتقل إلى شاشة التحقق من البريد الإلكتروني
                    _registrationStatus.postValue(true)
                } else {
                    Log.e("SignUpViewModel", "Failed to create Firebase user")
                    _registrationStatus.postValue(false)
                }

            } catch (e: Exception) {
                _registrationStatus.postValue(false)
                Log.e("SignUpViewModel", "Error creating user: ${e.message}")
                e.printStackTrace()
            }
        }
    }


}




