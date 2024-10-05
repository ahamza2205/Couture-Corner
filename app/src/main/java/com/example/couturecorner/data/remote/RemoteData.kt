package com.example.couturecorner.data.remote

import com.apollographql.apollo3.api.ApolloResponse
import com.example.couturecorner.network.ApolloClient
import com.graphql.GetCuponCodesQuery
import com.graphql.GetProductsQuery
import com.graphql.HomeProductsQuery
import com.graphql.UpdateCustomerMetafieldsMutation
import com.graphql.type.CustomerInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RemoteData @Inject constructor() : IremoteData {


    override  fun getProducts(): Flow<ApolloResponse<GetProductsQuery.Data>> = flow {

        val response = ApolloClient.apolloClient.query(GetProductsQuery()).execute()

        emit(response)

    }

    override fun getHomeProducts(): Flow<ApolloResponse<HomeProductsQuery.Data>> = flow{
       val response = ApolloClient.apolloClient.query(HomeProductsQuery()).execute()
        emit(response)
    }

    override fun getCupones(): Flow<ApolloResponse<GetCuponCodesQuery.Data>> = flow {
        val response = ApolloClient.apolloClient.query(GetCuponCodesQuery()).execute()
        emit(response)
    }

    // New method to update customer
    override fun updateCustomer(input: CustomerInput): Flow<ApolloResponse<UpdateCustomerMetafieldsMutation.Data>> = flow {
        val response = ApolloClient.apolloClient.mutation(
            UpdateCustomerMetafieldsMutation(input = input)
        ).execute()
        emit(response)
    }

}

