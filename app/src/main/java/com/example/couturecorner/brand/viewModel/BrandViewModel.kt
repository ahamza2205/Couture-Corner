package com.example.couturecorner.brand.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.ApolloResponse
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.data.repository.Irepo
import com.graphql.FilteredProductsQuery
import com.graphql.HomeProductsQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrandViewModel @Inject constructor(
    private val repo: Irepo
): ViewModel() {
    private val _productsBrands = MutableStateFlow<ApiState<ApolloResponse<FilteredProductsQuery.Data>>>(
        ApiState.Loading)
    val productsBrands : StateFlow<ApiState<ApolloResponse<FilteredProductsQuery.Data>>> =_productsBrands

    fun getFilterdProducts(vendor: String?) {
        viewModelScope.launch {
            repo.getFilterdProducts(vendor).collect {
                if (it.hasErrors())
                {
                    _productsBrands.value= ApiState.Error(it.errors?.get(0)?.message.toString())
                }
                else
                {
                    _productsBrands.value= ApiState.Success(it)
                }
            }
        }
    }
}