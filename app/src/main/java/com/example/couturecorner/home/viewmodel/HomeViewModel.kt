package com.example.couturecorner.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.ApolloResponse
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.data.repository.Irepo
import com.google.firebase.auth.FirebaseAuth

import com.graphql.GetProductsQuery
import com.graphql.HomeProductsQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel@Inject constructor(
    private val repo: Irepo
):ViewModel() {

    private val _products = MutableStateFlow<ApiState<ApolloResponse<HomeProductsQuery.Data>>>(ApiState.Loading)
    val products : StateFlow<ApiState<ApolloResponse<HomeProductsQuery.Data>>> =_products


    fun getProducts()
    {
        viewModelScope.launch {
            repo.getHomeProducts().collect{
                if (it.hasErrors())
                {
                    _products.value= ApiState.Error(it.errors?.get(0)?.message.toString())
                }
                else
                {
                    _products.value= ApiState.Success(it)
                }
            }
        }
    }

    // Method to add a product to favorites
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