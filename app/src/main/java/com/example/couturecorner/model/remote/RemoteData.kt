package com.example.couturecorner.model.remote

import com.apollographql.apollo3.api.ApolloResponse
import com.graphql.GetProductsQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RemoteData : IremoteData {

    override  fun getProducts(): Flow<ApolloResponse<GetProductsQuery.Data>> = flow {

        val response = ApolloClient.apolloClient.query(GetProductsQuery()).execute()

        emit(response)

    }

}

