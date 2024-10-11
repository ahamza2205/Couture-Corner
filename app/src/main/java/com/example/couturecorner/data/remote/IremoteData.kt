package com.example.couturecorner.data.remote

import com.apollographql.apollo3.api.ApolloResponse
import com.example.couturecorner.data.model.ApiState
import com.graphql.CreateOrderFromDraftOrderMutation
import com.graphql.DeleteDraftOrderMutation
import com.graphql.DraftOrderCreateMutation
import com.graphql.FilteredProductsQuery
import com.graphql.GetProductsQuery
import kotlinx.coroutines.flow.Flow
import com.graphql.GetCuponCodesQuery
import com.graphql.GetDraftOrdersByCustomerQuery
import com.graphql.HomeProductsQuery
import com.graphql.ProductQuery
import com.graphql.UpdateCustomerMetafieldsMutation
import com.graphql.UpdateDraftOrderMetafieldsMutation
import com.graphql.type.CustomerInput
import com.graphql.type.DraftOrderDeleteInput
import com.graphql.type.DraftOrderInput

interface IremoteData   {
    fun getProducts(): Flow<ApolloResponse<GetProductsQuery.Data>>
    fun getHomeProducts(): Flow<ApolloResponse<HomeProductsQuery.Data>>
    fun getFilterdProducts(vendor:String?): Flow<ApolloResponse<FilteredProductsQuery.Data>>
    fun getCupones(): Flow<ApolloResponse<GetCuponCodesQuery.Data>>
    fun updateCustomer(input: CustomerInput): Flow<ApolloResponse<UpdateCustomerMetafieldsMutation.Data>>
    fun createDraftOrder(input: DraftOrderInput): Flow<ApolloResponse<DraftOrderCreateMutation.Data>>
    fun getDraftOrderByCustomerId(id: String): Flow<ApolloResponse<GetDraftOrdersByCustomerQuery.Data>>
    fun deleteDraftOrder(input: DraftOrderDeleteInput): Flow<ApolloResponse<DeleteDraftOrderMutation.Data>>
    fun updateDraftOrder(input: DraftOrderInput, id: String): Flow<ApolloResponse<UpdateDraftOrderMetafieldsMutation.Data>>
    fun createOrderFromDraft(id: String): Flow<ApolloResponse<CreateOrderFromDraftOrderMutation.Data>>
    suspend fun getProductDetails(productId: String): Flow<ApiState<ProductQuery.Data?>>
    suspend fun addProductToFavorites(customerId: String, productId: String)
    suspend fun getCurrentFavorites(customerId: String): List<String>?
    suspend fun removeProductFromFavorites(customerId: String, productId: String)
}





