package com.example.couturecorner.data.repository

import android.util.Log
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.example.couturecorner.data.local.SharedPreference
import com.example.couturecorner.data.model.ApiState
import com.example.couturecorner.data.remote.IremoteData
import com.example.couturecorner.network.ApolloClient
import com.example.couturecorner.network.ApolloClient.apolloClient
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.graphql.AddFavoriteProductMutation
import com.graphql.CustomerCreateMutation
import com.graphql.FilteredProductsQuery
import com.graphql.GetCuponCodesQuery
import com.graphql.GetCustomerByIdQuery
import com.graphql.GetFavoriteProductsQuery
import com.graphql.GetProductsQuery
import com.graphql.HomeProductsQuery
import com.graphql.UpdateCustomerMetafieldsMutation
import com.graphql.ProductQuery
import com.graphql.type.Customer
import com.graphql.type.CustomerInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

class Repo
    @Inject constructor(
    private val remoteData: IremoteData,
    private val sharedPreference: SharedPreference

) : Irepo {

    // ---------------------------- Product  ------------------------------------

    override fun getProducts(): Flow<ApolloResponse<GetProductsQuery.Data>> {
        return remoteData.getProducts()
    }

    override fun getHomeProducts(): Flow<ApolloResponse<HomeProductsQuery.Data>> {
        return remoteData.getHomeProducts()
    }

    override fun getFilterdProducts(vendor: String?): Flow<ApolloResponse<FilteredProductsQuery.Data>> {
        return remoteData.getFilterdProducts(vendor)
    }


    // ---------------------------- shared preference ------------------------------------
    override fun saveUserLoggedIn(isLoggedIn: Boolean) {
        sharedPreference.saveUserLoggedIn(isLoggedIn)
    }

    override fun isUserLoggedIn(): Boolean {
        return sharedPreference.isUserLoggedIn()
    }

    override fun logoutUser() {
        sharedPreference.logoutUser()
    }


    // --------------------------- shopify registration -------------------------------
    suspend fun registerUser(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNumber: String
    ): String? {
        val auth = FirebaseAuth.getInstance()
        try {
            // Create Firebase user
            val firebaseUserId = auth.createUserWithEmailAndPassword(email, password).await()
            val user = FirebaseAuth.getInstance().currentUser
            val userEmail = user?.email
            if (firebaseUserId.user != null) {
                // Proceed with Shopify registration
                val client = ApolloClient.apolloClient
                val mutation = CustomerCreateMutation(
                    input = CustomerInput(
                        email = Optional.Present(email),
                        firstName = Optional.Present(firstName),
                        lastName = Optional.Present(lastName),
                        phone = Optional.Present(phoneNumber)
                    )
                )
                val response = client.mutation(mutation).execute()

                if (response.hasErrors()) {
                    // Log Shopify errors
                    Log.e("ShopifyRegistration", "Shopify user creation failed: ${response.errors}")
                    throw Exception("Error creating Shopify user: ${response.errors}")
                } else {
                    val shopifyUserId = response.data?.customerCreate?.customer?.id
                    Log.d("UserRegistration", "User successfully created on Shopify: $firstName $lastName, Shopify User ID: $shopifyUserId")

                    // Save the Shopify User ID in shared preferences
                    if (userEmail != null) {
                        sharedPreference.saveShopifyUserId( userEmail , shopifyUserId ?: "")
                    }
                    if (shopifyUserId != null) {
                        getCustomerById(shopifyUserId)
                    }
                    return shopifyUserId
                }
            } else {
                // Log Firebase registration failure
                Log.e("UserRegistration", "Failed to create Firebase user")
                throw Exception("Failed to create Firebase user")
            }
        } catch (e: Exception) {
            Log.e("UserRegistration", "Error in registration process: ${e.message}")
            throw e
        }
    }

    // Fetch customer data by ID
    suspend fun getCustomerById(customerId: String): GetCustomerByIdQuery.Customer? {
        val client = ApolloClient.apolloClient
        val response = client.query(GetCustomerByIdQuery(id = customerId)).execute()

        if (response.hasErrors()) {
            Log.e("CustomerData", "Error fetching customer: ${response.errors}")
            throw Exception("Error fetching customer: ${response.errors}")
        }

        response.data?.customer?.let { customer ->
            return GetCustomerByIdQuery.Customer(
                id = customer.id,
                displayName = customer.displayName ?: "",
                email = customer.email,
                firstName = customer.firstName,
                lastName = customer.lastName,
                phone = customer.phone,
                createdAt = customer.createdAt,
                updatedAt = customer.updatedAt
            )
        }
        return null
    }

    // ----------------------------------- product details --------------------------------
    override fun getProductDetails(productId: String): Flow<ApiState<ProductQuery.Data>> = flow {
        try {
            val response = apolloClient.query(ProductQuery(productId)).execute()
            emit(ApiState.Success(response.data!!))
        } catch (e: Exception) {
            emit(ApiState.Error(e.message ?: "Unknown Error"))
        }
    }

    // ------------------------ get & save shopify user id --------------------------------
    override fun getShopifyUserId(email: String): String? {
        return sharedPreference.getShopifyUserId(email)
    }

    override fun saveShopifyUserId(email: String, userId: String) {
        sharedPreference.saveShopifyUserId(email, userId)
    }

// --------------------------- Add product to favorite --------------------------------
override suspend fun addProductToFavorites(customerId: String, productId: String) {
    try {
        val currentFavorites = getCurrentFavorites(customerId) ?: listOf()
        val updatedFavorites = (currentFavorites + productId).distinct() // distinct avoid duplicates
        val jsonProductIds = Gson().toJson(updatedFavorites)
        val mutation = AddFavoriteProductMutation(
            customerId = customerId,
            productIds = jsonProductIds // Convert the list to a JSON string
        )
        val response = apolloClient.mutation(mutation).execute()
        // Check for errors
        if (response.data?.customerUpdate?.userErrors?.isNotEmpty() == true) {
            for (error in response.data!!.customerUpdate?.userErrors!!) {
                Log.e("AddFavorite", "User Error: ${error.message} for field ${error.field}")
            }
            return
        }
        // Check for successful operation
        if (response.hasErrors()) {
            throw Exception("Error adding product to favorites: ${response.errors}")
        } else {
            Log.d("AddFavorite", "Product added to favorites successfully")
        }
    } catch (e: Exception) {
        Log.e("AddFavorite", "Error: ${e.message}")
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
                Log.d("GetFavorites", "Found value: $value") // Log the raw value
                // Clean up the string by removing unwanted characters and extra brackets
                val cleanedValue = value?.replace("""[\[\]\\""]""".toRegex(), "") // Remove unwanted characters
                // Split the string by commas to get the product IDs
                cleanedValue?.split(",")?.map { it.trim() } ?: listOf()
            }
        } ?: listOf()

        Log.d("GetFavorites", "Current favorites: $favoriteProducts")
        return favoriteProducts
    }


    override fun getCupones(): Flow<ApolloResponse<GetCuponCodesQuery.Data>> {
        return remoteData.getCupones()
    }
    override fun updateCustomer(input: CustomerInput): Flow<ApolloResponse<UpdateCustomerMetafieldsMutation.Data>> {
        return remoteData.updateCustomer(input)
    }
}