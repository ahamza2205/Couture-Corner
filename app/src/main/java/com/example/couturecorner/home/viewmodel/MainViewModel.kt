package com.example.couturecorner.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.example.couturecorner.data.local.SharedPreference
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.data.repository.Irepo
import com.google.firebase.auth.FirebaseAuth
import com.graphql.GetProductsQuery
import com.graphql.type.CustomerInput
import com.graphql.type.MailingAddressInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repo: Irepo,
    val sharedPreference: SharedPreference
) : ViewModel() {

    private val _productsApollo = MutableStateFlow<ApiState<ApolloResponse<GetProductsQuery.Data>>>(
        ApiState.Loading)
    val productsApollo: StateFlow<ApiState<ApolloResponse<GetProductsQuery.Data>>> = _productsApollo


    private val _favIdsList= MutableStateFlow<List<String>>(emptyList())
    val favIdsList : StateFlow<List<String>> =_favIdsList

    var selectedProductId: String? = null

    fun getProducts() {
        viewModelScope.launch {
            repo.getProducts().collect {
                if (it.hasErrors()) {
                    _productsApollo.value = ApiState.Error(it.errors?.get(0)?.message.toString())
                } else {
                    _productsApollo.value = ApiState.Success(it)
                }
            }
        }
    }

    fun getFavList() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userEmail = user.email

            if (userEmail != null) {
                val customerId = sharedPreference.getShopifyUserId(userEmail)

                if (customerId != null) {
                    viewModelScope.launch {
                        val favs = repo.getCurrentFavorites(customerId)
                        _favIdsList.value= favs ?: emptyList()

                    }
                }
            }
        }

    }


    fun addProductToFavorites(productId: String) {
        viewModelScope.launch {
            // Get current Firebase user
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                val userEmail = user.email

                if (userEmail != null) {
                    try {
                        // Get the customer ID using the user's email
                        val customerId = repo.getShopifyUserId(userEmail)
                        if (customerId != null) {
                            // Attempt to add product to favorites in repo
                            repo.addProductToFavorites(customerId, productId)
                            // Log success for debugging purposes
                            Log.d("HomeViewModel", "Product $productId added to favorites for customer $customerId")
                        } else {
                            Log.e("HomeViewModel", "Error: customerId is null")
                        }
                    } catch (e: Exception) {
                        Log.e("HomeViewModel", "Error adding product to favorites: ${e.message}")
                    }
                } else {
                    Log.e("HomeViewModel", "Error: User email is null")
                }
            } else {
                Log.e("HomeViewModel", "Error: No user logged in")
            }
        }
    }
}
