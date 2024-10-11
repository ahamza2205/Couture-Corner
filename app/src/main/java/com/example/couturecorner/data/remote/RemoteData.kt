package com.example.couturecorner.data.remote

import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.example.couturecorner.network.ApolloClient
import com.graphql.CreateOrderFromDraftOrderMutation
import com.graphql.DeleteDraftOrderMutation
import com.graphql.DraftOrderCreateMutation

import com.graphql.FilteredProductsQuery
import com.graphql.GetCuponCodesQuery
import com.graphql.GetDraftOrdersByCustomerQuery
import com.graphql.GetOrdersByCustomerQuery
import com.graphql.GetProductsQuery
import com.graphql.HomeProductsQuery
import com.graphql.OrderByIdQuery
import com.graphql.UpdateCustomerMetafieldsMutation
import com.graphql.UpdateDraftOrderMetafieldsMutation
import com.graphql.type.CustomerInput
import com.graphql.type.DraftOrderDeleteInput
import com.graphql.type.DraftOrderInput
import com.sun.mail.imap.protocol.ID
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

    override fun getOrders(emai:String):Flow<ApolloResponse<GetOrdersByCustomerQuery.Data>> = flow {
        val response = ApolloClient.apolloClient.query(GetOrdersByCustomerQuery(emai)).execute()
        emit(response)
    }

    override fun getOrderById(id: String): Flow<ApolloResponse<OrderByIdQuery.Data>> = flow {

        val response = ApolloClient.apolloClient.query(OrderByIdQuery(id)).execute()
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

    override fun createDraftOrder(input: DraftOrderInput): Flow<ApolloResponse<DraftOrderCreateMutation.Data>> =flow{

        val response = ApolloClient.apolloClient.mutation(
            DraftOrderCreateMutation(input = input)
        ).execute()

        emit(response)
    }


    override fun createOrderFromDraft(
        id: String
    ): Flow<ApolloResponse<CreateOrderFromDraftOrderMutation.Data>> = flow {
        val response = ApolloClient.apolloClient.mutation(
            CreateOrderFromDraftOrderMutation(id)
        ).execute()
        emit(
            response
        )

    }

    override fun getDraftOrderByCustomerId(id: String): Flow<ApolloResponse<GetDraftOrdersByCustomerQuery.Data>> = flow {

        val response = ApolloClient.apolloClient.query(GetDraftOrdersByCustomerQuery(id)).execute()
        emit(response)
    }

    override fun deleteDraftOrder(input: DraftOrderDeleteInput): Flow<ApolloResponse<DeleteDraftOrderMutation.Data>> = flow {

        val response = ApolloClient.apolloClient.mutation(
            DeleteDraftOrderMutation(input = input)
        ).execute()
        emit(response)
    }


    override fun updateDraftOrder(input: DraftOrderInput, id: String): Flow<ApolloResponse<UpdateDraftOrderMetafieldsMutation.Data>> = flow {

        val response = ApolloClient.apolloClient.mutation(
            UpdateDraftOrderMetafieldsMutation(input = input, ownerId = id)
        ).execute()
        emit(response)
    }
    }



