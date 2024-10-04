package com.example.couturecorner.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.ApolloResponse
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.data.repository.Irepo

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
}