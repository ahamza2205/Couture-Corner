package com.example.couturecorner.data.remote

import com.apollographql.apollo3.api.ApolloResponse
import com.graphql.GetProductsQuery
import kotlinx.coroutines.flow.Flow

interface IremoteData   {
    fun getProducts(): Flow<ApolloResponse<GetProductsQuery.Data>>
}