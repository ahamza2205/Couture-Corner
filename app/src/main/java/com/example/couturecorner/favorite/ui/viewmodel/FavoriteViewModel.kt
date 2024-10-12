package com.example.couturecorner.favorite.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.data.repository.Repo
import com.example.couturecorner.network.MyApolloClient.apolloClient
import com.graphql.ProductQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteRepository: Repo
) : ViewModel() {
    private val _favoriteProducts = MutableLiveData<ApiState<List<ProductQuery.Product>>>()
    val favoriteProducts: LiveData<ApiState<List<ProductQuery.Product>>> get() = _favoriteProducts

    fun loadFavoriteProducts(customerId: String) {
        viewModelScope.launch {
            _favoriteProducts.value = ApiState.Loading
            try {
                val favoriteIds = favoriteRepository.getCurrentFavorites(customerId) ?: emptyList()
                val productDetailsFlow = favoriteIds.map { productId ->
                    getProductDetails(productId)
                }
                val productDetails = mutableListOf<ProductQuery.Product>()
                productDetailsFlow.forEach { flow ->
                    flow.collect { state ->
                        if (state is ApiState.Success) {
                            state.data?.let { productDetails.add(it) }
                        }
                    }
                }
                _favoriteProducts.value = ApiState.Success(productDetails)
            } catch (e: Exception) {
                _favoriteProducts.value = ApiState.Error(e.message ?: "Unknown Error")
            }
        }
    }
    fun removeProductFromFavorites(customerId: String, productId: String) {
        viewModelScope.launch {
            try {
                favoriteRepository.removeProductFromFavorites(customerId, productId)
                loadFavoriteProducts(customerId)
            } catch (e: Exception) {
                _favoriteProducts.value = ApiState.Error(e.message ?: "Unknown Error")
            }
            }
        }
    fun getProductDetails(productId: String): Flow<ApiState<ProductQuery.Product>> = flow {
        try {
            val response = apolloClient.query(ProductQuery(productId)).execute()
            emit(ApiState.Success(response.data!!.product))
        } catch (e: Exception) {
            emit(ApiState.Error(e.message ?: "Unknown Error"))
        }
    }
}




