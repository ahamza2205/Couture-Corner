package com.example.couturecorner.network

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import okhttp3.OkHttpClient

object ApolloClient {
        private const val BASE_URL = "https://android-alex-team2.myshopify.com/admin/api/2023-01/graphql.json"

        val apolloClient = ApolloClient.Builder()
            .serverUrl(BASE_URL)
            .okHttpClient(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val original = chain.request()
                        val request = original.newBuilder()
                            //put your token here
                            .build()
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()



}