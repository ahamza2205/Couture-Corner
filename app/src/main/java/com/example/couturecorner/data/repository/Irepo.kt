package com.example.couturecorner.data.repository

import com.apollographql.apollo3.api.ApolloResponse
import com.graphql.GetProductsQuery
import kotlinx.coroutines.flow.Flow

interface Irepo {
     fun getProducts(): Flow<ApolloResponse<GetProductsQuery.Data>>

     // --------------- shared preference  -------------------------

     fun saveUserLoggedIn(isLoggedIn: Boolean)
     fun isUserLoggedIn(): Boolean
     fun logoutUser()
}