package com.example.couturecorner.data.repository

import com.apollographql.apollo3.api.ApolloResponse
import com.example.couturecorner.data.model.ApiState
import com.graphql.FilteredProductsQuery
import com.graphql.GetCuponCodesQuery
import com.graphql.GetProductsQuery
import com.graphql.UpdateCustomerMetafieldsMutation
import com.graphql.type.CustomerInput
import com.graphql.HomeProductsQuery
import com.graphql.ProductQuery
import kotlinx.coroutines.flow.Flow

interface Irepo {
     // ---------------- api call ----------------
     fun getProducts(): Flow<ApolloResponse<GetProductsQuery.Data>>

    fun getCupones(): Flow<ApolloResponse<GetCuponCodesQuery.Data>>

    fun updateCustomer(input: CustomerInput): Flow<ApolloResponse<UpdateCustomerMetafieldsMutation.Data>>
     fun getHomeProducts(): Flow<ApolloResponse<HomeProductsQuery.Data>>
     fun getProductDetails(productId: String): Flow<ApiState<ProductQuery.Data>>
     fun getFilterdProducts(vendor:String?): Flow<ApolloResponse<FilteredProductsQuery.Data>>

     // --------------- shared preference  -------------------------

     fun saveUserLoggedIn(isLoggedIn: Boolean)
     fun isUserLoggedIn(): Boolean
     fun logoutUser()
     fun getShopifyUserId(email: String): String?
     fun saveShopifyUserId(email: String, userId: String)
     // -------------- add product to favorite ------------------------
     suspend fun addProductToFavorites(customerId: String, productId: String)
     fun saveAddressState(haveAddress: Boolean)
     fun getAddressState(): Boolean
}