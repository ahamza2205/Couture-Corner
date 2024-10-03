package com.example.couturecorner.model.remote

import com.apollographql.apollo3.api.ApolloResponse
import com.graphql.GetProductsQuery
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface IremoteData   {
    fun getProducts(): Flow<ApolloResponse<GetProductsQuery.Data>>
}