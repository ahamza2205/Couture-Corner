package com.example.couturecorner.network

import com.example.couturecorner.data.remote.paypall.PayPalService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api-m.sandbox.paypal.com/"

    val instance: PayPalService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)

            .addConverterFactory(GsonConverterFactory.create())

            .build()

        retrofit.create(PayPalService::class.java)
    }
}