package com.example.couturecorner.data.remote

import android.util.Log
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.example.couturecorner.data.local.SharedPreference
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.network.MyApolloClient
import com.example.couturecorner.network.MyApolloClient.apolloClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson
import com.graphql.AddFavoriteProductMutation
import com.graphql.CreateOrderFromDraftOrderMutation
import com.graphql.CustomerCreateMutation
import com.graphql.DeleteDraftOrderMutation
import com.graphql.DraftOrderCreateMutation

import com.graphql.FilteredProductsQuery
import com.graphql.GetCuponCodesQuery
import com.graphql.GetCustomerByEmailQuery
import com.graphql.GetCustomerByIdQuery
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
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RemoteData @Inject constructor(
    val apolloClient: ApolloClient,
    val sharedPreference: SharedPreference
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

    override suspend fun registerUser(
        email: String?,
        password: String?,
        firstName: String?,
        lastName: String?,
        phoneNumber: String?,
        idToken: String?,
    ): String? {
        if (email.isNullOrEmpty() || firstName.isNullOrEmpty() || lastName.isNullOrEmpty()) {
            throw IllegalArgumentException("All fields except password and phoneNumber must be provided.")
        }

        val auth = FirebaseAuth.getInstance()
        try {
            // Create Firebase user
            val firebaseUserId = if (password != null) {
                auth.createUserWithEmailAndPassword(email, password).await()
            } else if (idToken != null) {
                // Sign in using Google
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()
            } else {
                throw IllegalArgumentException("Must provide either a password or an idToken for registration.")
            }

            if (firebaseUserId.user != null) {
                // Proceed with Shopify registration
                val client = MyApolloClient.apolloClient
                val mutation = CustomerCreateMutation(
                    input = CustomerInput(
                        email = Optional.Present(email),
                        firstName = Optional.Present(firstName),
                        lastName = Optional.Present(lastName),
                        phone = if (!phoneNumber.isNullOrEmpty()) Optional.Present(phoneNumber) else Optional.Absent,
                        tags = Optional.Absent,
                        metafields = Optional.Absent,
                        addresses = Optional.Absent
                    )
                )
                val response = client.mutation(mutation).execute()

                if (response.hasErrors()) {
                    Log.e("ShopifyRegistration", "Shopify user creation failed: ${response.errors}")
                    throw Exception("Error creating Shopify user: ${response.errors}")
                } else {
                    val shopifyUserId = response.data?.customerCreate?.customer?.id
                    Log.d("UserRegistration", "User successfully created on Shopify: $firstName $lastName, Shopify User ID: $shopifyUserId")

                    // Fetch the customer ID by email
                    val customerId = getCustomerByEmail(email)
                    if (customerId != null) {
                        // Save the customer ID in shared preferences
                        sharedPreference.saveShopifyUserId(email, customerId)
                        Log.d("UserRegistration", "Shopify User ID saved in SharedPreferences: $customerId")
                    } else {
                        Log.e("UserRegistration", "Failed to fetch Shopify user ID")
                    }

                    return shopifyUserId
                }
            } else {
                Log.e("UserRegistration", "Failed to create Firebase user")
                throw Exception("Failed to create Firebase user")
            }
        } catch (e: Exception) {
            Log.e("UserRegistration", "Error in registration process: ${e.message}")
            throw e
        }
    }
    override suspend fun getCustomerByEmail(email: String): String? {
        val client = MyApolloClient.apolloClient
        val response = client.query(GetCustomerByEmailQuery(email = email)).execute()

        if (response.hasErrors()) {
            Log.e("GetCustomerByEmail", "Error fetching customer by email: ${response.errors}")
            throw Exception("Error fetching customer by email: ${response.errors}")
        }

        // Extract customer ID from the response
        val customerId = response.data?.customers?.edges?.firstOrNull()?.node?.id
        return customerId
    }


    override suspend fun getCustomerById(customerId: String): GetCustomerByIdQuery.Customer? {
        val client = MyApolloClient.apolloClient
        val response = client.query(GetCustomerByIdQuery(id = customerId)).execute()

        if (response.hasErrors()) {
            Log.e("CustomerData", "Error fetching customer: ${response.errors}")
            throw Exception("Error fetching customer: ${response.errors}")
        }

        return response.data?.customer?.let { customer ->
            GetCustomerByIdQuery.Customer(
                id = customer.id,
                displayName = customer.displayName ?: "", // Handle nullable fields
                email = customer.email,
                firstName = customer.firstName,
                lastName = customer.lastName,
                phone = customer.phone,
                createdAt = customer.createdAt,
                updatedAt = customer.updatedAt,
                defaultAddress = customer.defaultAddress?.let { address ->
                    GetCustomerByIdQuery.DefaultAddress( // Correctly reference the nested DefaultAddress class
                        address1 = address.address1 ?: "", // Handle nullable fields
                        address2 = address.address2 ?: "",
                        city = address.city ?: "",
                        phone = address.phone ?: ""
                    )
                },
                // Add the addresses parameter, handle it as per its type (nullable or non-nullable)
                addresses = customer.addresses ?: emptyList() // Provide a default value if it is nullable
            )
        }
    }

}



