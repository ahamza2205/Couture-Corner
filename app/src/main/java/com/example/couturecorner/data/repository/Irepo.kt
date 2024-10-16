package com.example.couturecorner.data.repository

import com.apollographql.apollo3.api.ApolloResponse
import com.example.couturecorner.data.model.ApiState
import com.graphql.CreateOrderFromDraftOrderMutation
import com.graphql.DeleteDraftOrderMutation
import com.graphql.DraftOrderCreateMutation
import com.graphql.FilteredProductsQuery
import com.graphql.GetCuponCodesQuery
import com.graphql.GetCustomerByIdQuery
import com.graphql.GetDraftOrdersByCustomerQuery
import com.graphql.GetOrdersByCustomerQuery
import com.graphql.GetProductsQuery
import com.graphql.UpdateCustomerMetafieldsMutation
import com.graphql.type.CustomerInput
import com.graphql.HomeProductsQuery
import com.graphql.OrderByIdQuery
import com.graphql.ProductQuery
import com.graphql.UpdateDraftOrderMetafieldsMutation
import com.graphql.type.DraftOrderDeleteInput
import com.graphql.type.DraftOrderInput
import kotlinx.coroutines.flow.Flow

interface Irepo {
    // ---------------- api call ----------------
    fun getProducts(): Flow<ApolloResponse<GetProductsQuery.Data>>

    fun getCupones(): Flow<ApolloResponse<GetCuponCodesQuery.Data>>

    fun updateCustomer(input: CustomerInput): Flow<ApolloResponse<UpdateCustomerMetafieldsMutation.Data>>

    fun getHomeProducts(): Flow<ApolloResponse<HomeProductsQuery.Data>>

    suspend fun getProductDetails(productId: String): Flow<ApiState<ProductQuery.Data?>>

    fun getFilterdProducts(vendor: String?): Flow<ApolloResponse<FilteredProductsQuery.Data>>

    fun createDraftOrder(input: DraftOrderInput): Flow<ApolloResponse<DraftOrderCreateMutation.Data>>

    fun getDraftOrderByCustomerId(id: String): Flow<ApolloResponse<GetDraftOrdersByCustomerQuery.Data>>

    fun deleteDraftOrder(input: DraftOrderDeleteInput): Flow<ApolloResponse<DeleteDraftOrderMutation.Data>>

    fun updateDraftOrder(input: DraftOrderInput, id: String): Flow<ApolloResponse<UpdateDraftOrderMetafieldsMutation.Data>>
    fun createOrderFromDraft(id: String): Flow<ApolloResponse<CreateOrderFromDraftOrderMutation.Data>>

    fun getOrders(emai:String):Flow<ApolloResponse<GetOrdersByCustomerQuery.Data>>
    fun getOrderById(id:String):Flow<ApolloResponse<OrderByIdQuery.Data>>

    suspend fun removeProductFromFavorites(customerId: String, productId: String)

    // --------------- shared preference  -------------------------
    fun saveUserLoggedIn(isLoggedIn: Boolean)

    fun isUserLoggedIn(): Boolean

    fun logoutUser()

    fun getShopifyUserId(email: String): String?

    fun saveShopifyUserId(email: String, userId: String)

    fun saveDraftOrderTag(userId: String, tag: String)

    fun getDraftOrderTag(userId: String): String?

    fun saveDraftOrderId(userId: String, ID: String)

    fun getDraftOrderId(userId: String): String?

    fun deleteDraftOrderId(userId: String)

    // -------------- add product to favorite ------------------------
    suspend fun addProductToFavorites(customerId: String, productId: String)

    suspend fun getCurrentFavorites(customerId: String): List<String>?
    suspend fun registerUser(
        email: String?,
        password: String?,
        firstName: String?,
        lastName: String?,
        phoneNumber: String?,
        idToken: String? = null,
    ): String?
    suspend fun getCustomerByEmail(email: String): String?
    suspend fun getCustomerById(customerId: String): GetCustomerByIdQuery.Customer?
}
