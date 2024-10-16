package com.example.couturecorner.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.ApolloResponse
import com.example.couturecorner.data.local.SharedPreferenceImp
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.data.repository.Irepo
import com.example.couturecorner.data.repository.Repo
import com.google.firebase.auth.FirebaseAuth
import com.graphql.GetProductsQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repo: Irepo,
    private val currencyRepo: Repo,
    val sharedPreference: SharedPreferenceImp
) : ViewModel() {

    private val _productsApollo = MutableStateFlow<ApiState<ApolloResponse<GetProductsQuery.Data>>>(
        ApiState.Loading)
    val productsApollo: StateFlow<ApiState<ApolloResponse<GetProductsQuery.Data>>> = _productsApollo


    private val _favIdsList= MutableStateFlow<List<String>>(emptyList())
    val favIdsList : StateFlow<List<String>> =_favIdsList

    private val _convertedCurrency = MutableStateFlow<Map<String, Double?>>(emptyMap())
    val convertedCurrency: StateFlow<Map<String, Double?>> = _convertedCurrency

    private val _convertedCurrencyTotal = MutableStateFlow<Double>(0.0)
    val convertedCurrencyTotal: StateFlow<Double> = _convertedCurrencyTotal

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


    fun removeProductFromFavorites(productId: String)
    {
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
                           repo.removeProductFromFavorites(customerId, productId)
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

    fun convertCurrency(from: String, to: String, amount: Double, productId: String) {
        viewModelScope.launch {
            try {
                val conversionResult = currencyRepo.convertCurrency(from, to, amount, "9eabc320c6-66b069c4e1-sl3z9w")
                val result = conversionResult?.result?.get(to)
                _convertedCurrency.value = _convertedCurrency.value?.toMutableMap()?.apply {
                    put(productId, result)
                } ?: mapOf(productId to result)
            } catch (e: Exception) {
                _convertedCurrency.value = emptyMap()

            }
        }
    }

    fun covertCurrencyWithoutId(from: String, to: String, amount: Double)
    {
        viewModelScope.launch {
            val conversionResult = currencyRepo.convertCurrency(from, to, amount, "9eabc320c6-66b069c4e1-sl3z9w")
            val result = conversionResult?.result?.get(to)
            _convertedCurrencyTotal.value= result?:0.0
        }
    }

    fun getSelectedCurrency(): String? {
        return sharedPreference.getSelectedCurrency()
    }

}
