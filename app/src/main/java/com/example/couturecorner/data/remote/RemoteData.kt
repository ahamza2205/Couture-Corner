package com.example.couturecorner.data.remote

import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.example.couturecorner.network.ApolloClient

import com.graphql.FilteredProductsQuery
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


    override fun getFilterdProducts(vendor: String?): Flow<ApolloResponse<FilteredProductsQuery.Data>> = flow{

        val response = if (vendor == null) {
            ApolloClient.apolloClient.query(FilteredProductsQuery(query = Optional.Present(null))).execute()
        } else {
            ApolloClient.apolloClient.query(FilteredProductsQuery(query = Optional.Present(vendor))).execute()
        }
        emit(response)

//      val response = ApolloClient.apolloClient.query(FilteredProductsQuery(query =vendor)).execute()
//        emit(response)
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

