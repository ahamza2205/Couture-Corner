package com.example.couturecorner.network

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloRequest
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.interceptor.ApolloInterceptor
import com.apollographql.apollo3.interceptor.ApolloInterceptorChain
import com.apollographql.apollo3.network.okHttpClient
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.logging.Logger

object MyApolloClient {
        private const val BASE_URL = "https://android-alex-team2.myshopify.com/admin/api/2023-01/graphql.json"

    // logger for debugging
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }


        val apolloClient = ApolloClient.Builder()
            .serverUrl(BASE_URL)
            .okHttpClient(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val original = chain.request()
                        val request = original.newBuilder()
                            .header("X-Shopify-Access-Token", "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")
                            .build()
                        chain.proceed(request)
                    }
                    //remove if you want to delete the logger
                    .addInterceptor(loggingInterceptor)
                    .build()
            )
            .build()



}