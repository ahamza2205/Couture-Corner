package com.example.couturecorner.data.repository

import android.util.Log
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.example.couturecorner.data.local.SharedPreference
import com.example.couturecorner.data.remote.IremoteData
import com.example.couturecorner.network.ApolloClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.graphql.CustomerCreateMutation
import com.graphql.GetProductsQuery
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
    suspend fun registerUser(email: String, password: String, firstName: String, lastName: String, phoneNumber: String) {
        val auth = FirebaseAuth.getInstance()
        val firebaseUserId = auth.createUserWithEmailAndPassword(email, password).await()

        firebaseUserId.user?.sendEmailVerification()?.await()

        if (firebaseUserId.user != null) {


            val client = ApolloClient.apolloClient
            val mutation = CustomerCreateMutation(
                input = CustomerInput(
                    email = email,
                    firstName = firstName,
                    lastName = lastName,
                    phone = Optional.Present(phoneNumber)
                )
            )

            Log.d("UserRegistration", "Attempting to create Shopify user: $firstName $lastName, Email: $email, Phone: $phoneNumber")

            val response = client.mutation(mutation).execute()

            if (response.hasErrors()) {
                Log.e("UserRegistration", "Error creating Shopify user: ${response.errors}")
                throw Exception("Error creating Shopify user: ${response.errors}")
            } else {
                val shopifyUserId = response.data?.customerCreate?.customer?.id
                Log.d("UserRegistration", "User successfully created on Shopify: $firstName $lastName, Shopify User ID: $shopifyUserId")
            }
        }
    }


}