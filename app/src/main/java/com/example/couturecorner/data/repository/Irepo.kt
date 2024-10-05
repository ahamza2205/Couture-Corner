package com.example.couturecorner.data.repository

import com.apollographql.apollo3.api.ApolloResponse
import com.graphql.GetCuponCodesQuery
import com.graphql.GetProductsQuery
import com.graphql.UpdateCustomerMetafieldsMutation
import com.graphql.type.CustomerInput
import kotlinx.coroutines.flow.Flow

interface Irepo {
     fun getProducts(): Flow<ApolloResponse<GetProductsQuery.Data>>

    fun getCupones(): Flow<ApolloResponse<GetCuponCodesQuery.Data>>

    fun updateCustomer(input: CustomerInput): Flow<ApolloResponse<UpdateCustomerMetafieldsMutation.Data>>
}