package com.example.couturecorner.data.repository

import com.apollographql.apollo3.api.ApolloResponse
import com.example.couturecorner.data.local.SharedPreference
import com.example.couturecorner.data.remote.IremoteData
import com.graphql.GetProductsQuery
import kotlinx.coroutines.flow.Flow
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

}