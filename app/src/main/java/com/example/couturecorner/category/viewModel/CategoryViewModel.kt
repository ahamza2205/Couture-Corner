package com.example.couturecorner.category.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.ApolloResponse
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.data.repository.Irepo
import com.graphql.FilteredProductsQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    val repo :Irepo
):ViewModel(){

    private val _productsCategory = MutableStateFlow<ApiState<ApolloResponse<FilteredProductsQuery.Data>>>(
        ApiState.Loading)
    val productsCategory : StateFlow<ApiState<ApolloResponse<FilteredProductsQuery.Data>>> =_productsCategory


    fun getFilterdProducts(cat: String) {
        viewModelScope.launch {
            repo.getFilterdProducts(cat).collect {
                if (it.hasErrors())
                {
                    _productsCategory.value= ApiState.Error(it.errors?.get(0)?.message.toString())
                }
                else
                {
                    _productsCategory.value= ApiState.Success(it)
                }
            }
        }
    }

}