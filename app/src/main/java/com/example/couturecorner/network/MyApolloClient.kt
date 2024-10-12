package com.example.couturecorner.network

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import okhttp3.OkHttpClient

object MyApolloClient {
        private const val BASE_URL = "https://android-alex-team2.myshopify.com/admin/api/2023-01/graphql.json"

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
                    .build()
            )
            .build()



}