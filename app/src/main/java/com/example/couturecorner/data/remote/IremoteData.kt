package com.example.couturecorner.data.remote

import com.apollographql.apollo3.api.ApolloResponse
import com.graphql.FilteredProductsQuery
import com.graphql.GetProductsQuery
import com.graphql.HomeProductsQuery
import kotlinx.coroutines.flow.Flow

interface IremoteData   {
    fun getProducts(): Flow<ApolloResponse<GetProductsQuery.Data>>
    fun getHomeProducts(): Flow<ApolloResponse<HomeProductsQuery.Data>>
    fun getFilterdProducts(vendor:String): Flow<ApolloResponse<FilteredProductsQuery.Data>>
}