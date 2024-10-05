package com.example.couturecorner.data.remote

import com.apollographql.apollo3.api.ApolloResponse
import com.graphql.GetProductsQuery
import kotlinx.coroutines.flow.Flow
import com.graphql.GetCuponCodesQuery
import com.graphql.HomeProductsQuery
import com.graphql.UpdateCustomerMetafieldsMutation
import com.graphql.type.CustomerInput

interface IremoteData   {
    fun getProducts(): Flow<ApolloResponse<GetProductsQuery.Data>>
    fun getHomeProducts(): Flow<ApolloResponse<HomeProductsQuery.Data>>


    fun getCupones(): Flow<ApolloResponse<GetCuponCodesQuery.Data>>
    fun updateCustomer(input: CustomerInput): Flow<ApolloResponse<UpdateCustomerMetafieldsMutation.Data>>


    }