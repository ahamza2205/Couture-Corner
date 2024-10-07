package com.example.couturecorner.data.repository

import android.util.Log
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.example.couturecorner.data.local.SharedPreference
import com.example.couturecorner.data.remote.IremoteData
import com.example.couturecorner.network.ApolloClient
import com.google.firebase.auth.FirebaseAuth
import com.graphql.CustomerCreateMutation
import com.graphql.FilteredProductsQuery
import com.graphql.GetCuponCodesQuery
import com.graphql.GetCustomerByIdQuery
import com.graphql.GetProductsQuery
import com.graphql.HomeProductsQuery
import com.graphql.UpdateCustomerMetafieldsMutation
import com.graphql.type.CustomerInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

class Repo
    @Inject constructor(
    private val remoteData: IremoteData,
    private val sharedPreference: SharedPreference

) : Irepo {

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
    suspend fun registerUser(email: String, password: String, firstName: String, lastName: String, phoneNumber: String): String? {
        val auth = FirebaseAuth.getInstance()
        try {
            val firebaseUserId = auth.createUserWithEmailAndPassword(email, password).await()

            if (firebaseUserId.user != null) {
                val client = ApolloClient.apolloClient
                val mutation = CustomerCreateMutation(
                    input = CustomerInput(

                        email = Optional.Present(email),
                        firstName = Optional.present(firstName),
                        lastName = Optional.Present(lastName),
                        phone = Optional.Present(phoneNumber)
                    )
                )
                val response = client.mutation(mutation).execute()

                if (response.hasErrors()) {
                    throw Exception("Error creating Shopify user: ${response.errors}")
                } else {
                    val shopifyUserId = response.data?.customerCreate?.customer?.id
                    Log.d("UserRegistration", "User successfully created on Shopify: $firstName $lastName, Shopify User ID: $shopifyUserId")

                    // Save the Shopify User ID in shared preferences
                    sharedPreference.saveShopifyUserId(shopifyUserId ?: "")
                    if (shopifyUserId != null) {
                        getCustomerById(shopifyUserId)
                    }
                    return shopifyUserId
                }
            } else {
                throw Exception("Failed to create Firebase user")
            }
        } catch (e: Exception) {
            Log.e("UserRegistration", "Error in registration process: ${e.message}")
            throw e
        }
    }

    suspend fun getCustomerById(customerId: String): GetCustomerByIdQuery.Customer? {
        val client = ApolloClient.apolloClient
        val response = client.query(GetCustomerByIdQuery(id = customerId)).execute()

        if (response.hasErrors()) {
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

    override fun getCupones(): Flow<ApolloResponse<GetCuponCodesQuery.Data>> {
        return remoteData.getCupones()
    }
    override fun updateCustomer(input: CustomerInput): Flow<ApolloResponse<UpdateCustomerMetafieldsMutation.Data>> {
        return remoteData.updateCustomer(input)
    }
}