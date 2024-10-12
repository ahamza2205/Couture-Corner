package com.example.couturecorner.data.remote

import android.util.Log
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.network.MyApolloClient
import com.example.couturecorner.network.MyApolloClient.apolloClient
import com.google.gson.Gson
import com.graphql.AddFavoriteProductMutation
import com.graphql.CreateOrderFromDraftOrderMutation
import com.graphql.DeleteDraftOrderMutation
import com.graphql.DraftOrderCreateMutation

import com.graphql.FilteredProductsQuery
import com.graphql.GetCuponCodesQuery
import com.graphql.GetDraftOrdersByCustomerQuery
import com.graphql.GetFavoriteProductsQuery
import com.graphql.GetOrdersByCustomerQuery
import com.graphql.GetProductsQuery
import com.graphql.HomeProductsQuery
import com.graphql.ProductQuery
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

class RemoteData @Inject constructor(
    val apolloClient: ApolloClient
) : IremoteData {


    override  fun getProducts(): Flow<ApolloResponse<GetProductsQuery.Data>> = flow {

        val response = MyApolloClient.apolloClient.query(GetProductsQuery()).execute()

        emit(response)

    }

    override fun getHomeProducts(): Flow<ApolloResponse<HomeProductsQuery.Data>> = flow{
       val response = MyApolloClient.apolloClient.query(HomeProductsQuery()).execute()
        emit(response)
    }


    override fun getFilterdProducts(vendor: String?): Flow<ApolloResponse<FilteredProductsQuery.Data>> = flow{

        val response = if (vendor == null) {
            MyApolloClient.apolloClient.query(FilteredProductsQuery(query = Optional.Present(null))).execute()
        } else {
            MyApolloClient.apolloClient.query(FilteredProductsQuery(query = Optional.Present(vendor))).execute()
        }
        emit(response)

//      val response = ApolloClient.apolloClient.query(FilteredProductsQuery(query =vendor)).execute()
//        emit(response)
    }

    override fun getOrders(emai:String):Flow<ApolloResponse<GetOrdersByCustomerQuery.Data>> = flow {
        val response = MyApolloClient.apolloClient.query(GetOrdersByCustomerQuery(emai)).execute()
        emit(response)
    }

    override fun getOrderById(id: String): Flow<ApolloResponse<OrderByIdQuery.Data>> = flow {

        val response = MyApolloClient.apolloClient.query(OrderByIdQuery(id)).execute()
        emit(response)
    }


    override fun getCupones(): Flow<ApolloResponse<GetCuponCodesQuery.Data>> = flow {
        val response = MyApolloClient.apolloClient.query(GetCuponCodesQuery()).execute()
        emit(response)
    }

    // New method to update customer
    override fun updateCustomer(input: CustomerInput): Flow<ApolloResponse<UpdateCustomerMetafieldsMutation.Data>> = flow {
        val response = MyApolloClient.apolloClient.mutation(
            UpdateCustomerMetafieldsMutation(input = input)
        ).execute()
        emit(response)
    }

    override fun createDraftOrder(input: DraftOrderInput): Flow<ApolloResponse<DraftOrderCreateMutation.Data>> =flow{

        val response = MyApolloClient.apolloClient.mutation(
            DraftOrderCreateMutation(input = input)
        ).execute()

        emit(response)
    }


    override fun createOrderFromDraft(
        id: String
    ): Flow<ApolloResponse<CreateOrderFromDraftOrderMutation.Data>> = flow {
        val response = MyApolloClient.apolloClient.mutation(
            CreateOrderFromDraftOrderMutation(id)
        ).execute()
        emit(
            response
        )

    }

    override fun getDraftOrderByCustomerId(id: String): Flow<ApolloResponse<GetDraftOrdersByCustomerQuery.Data>> = flow {

        val response = MyApolloClient.apolloClient.query(GetDraftOrdersByCustomerQuery(id)).execute()
        emit(response)
    }

    override fun deleteDraftOrder(input: DraftOrderDeleteInput): Flow<ApolloResponse<DeleteDraftOrderMutation.Data>> = flow {

        val response = MyApolloClient.apolloClient.mutation(
            DeleteDraftOrderMutation(input = input)
        ).execute()
        emit(response)
    }


    override fun updateDraftOrder(input: DraftOrderInput, id: String): Flow<ApolloResponse<UpdateDraftOrderMetafieldsMutation.Data>> = flow {

        val response = MyApolloClient.apolloClient.mutation(
            UpdateDraftOrderMetafieldsMutation(input = input, ownerId = id)
        ).execute()
        emit(response)
    }


    override  suspend fun getProductDetails(productId: String): Flow<ApiState<ProductQuery.Data?>> = flow {
        try {
            val query = ProductQuery(productId)
            val response = apolloClient.query(query).execute()
            val data = response.data
            emit(ApiState.Success(data))
        } catch (e: Exception) {
            emit(ApiState.Error(e.message ?: "Unknown Error"))  // Use the exception message
        }
    }
   // ------------------------- add product to favorite ------------------------
    override suspend fun addProductToFavorites(customerId: String, productId: String) {
        try {
            // Call getCurrentFavorites directly
            val currentFavorites = getCurrentFavorites(customerId) ?: listOf()
            val updatedFavorites = (currentFavorites + productId).distinct() // distinct to avoid duplicates
            val jsonProductIds = Gson().toJson(updatedFavorites)

            val mutation = AddFavoriteProductMutation(
                customerId = customerId,
                productIds = jsonProductIds // Convert the list to a JSON string
            )

            val response = apolloClient.mutation(mutation).execute()

            // Check for errors
            if (response.data?.customerUpdate?.userErrors?.isNotEmpty() == true) {
                response.data!!.customerUpdate?.userErrors?.forEach { error ->
                }
                return
            }

            // Check for successful operation
            if (response.hasErrors()) {
                throw Exception("Error adding product to favorites: ${response.errors}")
            } else {
            }
        } catch (e: Exception) {
        }
    }
    override suspend fun getCurrentFavorites(customerId: String): List<String>? {
        val query = GetFavoriteProductsQuery(customerId = customerId)
        val response = apolloClient.query(query).execute()
        if (response.hasErrors()) {
            throw Exception("Error fetching current favorites: ${response.errors}")
        }
        val favoriteProducts = response.data?.customer?.metafields?.edges?.let { edges ->
            edges.flatMap {
                val value = it?.node?.value
                // Clean up the string by removing unwanted characters and extra brackets
                val cleanedValue = value?.replace("""[\[\]\\""]""".toRegex(), "") // Remove unwanted characters
                // Split the string by commas to get the product IDs
                cleanedValue?.split(",")?.map { it.trim() } ?: listOf()
            }
        } ?: listOf()

        return favoriteProducts
    }

    override suspend fun removeProductFromFavorites(customerId: String, productId: String) {
        try {
            val currentFavorites = getCurrentFavorites(customerId) ?: listOf()
            // Remove the productId from the favorites list
            val updatedFavorites = currentFavorites.filter { it != productId }
            val jsonProductIds = Gson().toJson(updatedFavorites)

            val mutation = AddFavoriteProductMutation(
                customerId = customerId,
                productIds = jsonProductIds // Convert the updated list to a JSON string
            )

            val response = apolloClient.mutation(mutation).execute()

            // Check for errors
            if (response.data?.customerUpdate?.userErrors?.isNotEmpty() == true) {
                for (error in response.data!!.customerUpdate?.userErrors!!) {
                }
                return
            }

            // Check for successful operation
            if (response.hasErrors()) {
                throw Exception("Error removing product from favorites: ${response.errors}")
            } else {
            }
        } catch (e: Exception) {
        }
    }
}



