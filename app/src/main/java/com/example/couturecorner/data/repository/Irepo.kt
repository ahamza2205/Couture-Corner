package com.example.couturecorner.data.repository

import com.apollographql.apollo3.api.ApolloResponse
import com.example.couturecorner.data.model.ApiState
import com.graphql.GetProductsQuery
import com.graphql.HomeProductsQuery
import com.graphql.ProductQuery
import kotlinx.coroutines.flow.Flow

interface Irepo {
     // ---------------- api call ----------------
     fun getProducts(): Flow<ApolloResponse<GetProductsQuery.Data>>
     fun getHomeProducts(): Flow<ApolloResponse<HomeProductsQuery.Data>>
     fun getProductDetails(productId: String): Flow<ApiState<ProductQuery.Data>>
     // --------------- shared preference  -------------------------
     fun saveUserLoggedIn(isLoggedIn: Boolean)
     fun isUserLoggedIn(): Boolean
     fun logoutUser()
     fun getShopifyUserId(email: String): String?
     fun saveShopifyUserId(email: String, userId: String)
     // -------------- add product to favorite ------------------------
     suspend fun addProductToFavorites(customerId: String, productId: String)
}