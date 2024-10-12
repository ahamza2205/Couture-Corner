package com.example.couturecorner.productdetails.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.data.repository.Repo
import com.graphql.ProductQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(private val repository: Repo ) : ViewModel() {
    private val _productDetails = MutableStateFlow<ApiState<ProductQuery.Data>>(ApiState.Loading)
    val productDetails: StateFlow<ApiState<ProductQuery.Data>> = _productDetails

    fun getProductDetails(productId: String) {
        viewModelScope.launch {
            repository.getProductDetails(productId).collect {
                _productDetails.value = it as ApiState<ProductQuery.Data>
            }
        }
    }
}
