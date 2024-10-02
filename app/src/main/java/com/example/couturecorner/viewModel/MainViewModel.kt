package com.example.couturecorner.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.ApolloResponse
import com.example.couturecorner.model.ApiState
import com.example.couturecorner.model.Irepo
import com.graphql.GetProductsQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(var repo:Irepo) : ViewModel() {

    private val _productsApollo = MutableStateFlow<ApiState<ApolloResponse<GetProductsQuery.Data>>>(ApiState.Loading)
    val productsApollo : StateFlow<ApiState<ApolloResponse<GetProductsQuery.Data>>> =_productsApollo




    fun getProducts(){
        viewModelScope.launch {
            repo.getProducts().collect{
                if (it.hasErrors())
                {
                    _productsApollo.value=ApiState.Error(it.errors?.get(0)?.message.toString())
                }
                else
                {
                    _productsApollo.value=ApiState.Success(it)
                }
            }
        }
    }

}