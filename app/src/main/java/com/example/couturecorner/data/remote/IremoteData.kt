package com.example.couturecorner.data.remote

import com.apollographql.apollo3.api.ApolloResponse
import com.graphql.CreateOrderFromDraftOrderMutation
import com.graphql.DeleteDraftOrderMutation
import com.graphql.DraftOrderCreateMutation
import com.graphql.FilteredProductsQuery
import com.graphql.GetProductsQuery
import kotlinx.coroutines.flow.Flow
import com.graphql.GetCuponCodesQuery
import com.graphql.GetDraftOrdersByCustomerQuery
import com.graphql.GetOrdersByCustomerQuery
import com.graphql.HomeProductsQuery
import com.graphql.OrderByIdQuery
import com.graphql.UpdateCustomerMetafieldsMutation
import com.graphql.UpdateDraftOrderMetafieldsMutation
import com.graphql.type.CustomerInput
import com.graphql.type.DraftOrderDeleteInput
import com.graphql.type.DraftOrderInput
import com.sun.mail.imap.protocol.ID

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
    fun getOrders(emai:String):Flow<ApolloResponse<GetOrdersByCustomerQuery.Data>>
    fun getOrderById(id:String):Flow<ApolloResponse<OrderByIdQuery.Data>>

}





