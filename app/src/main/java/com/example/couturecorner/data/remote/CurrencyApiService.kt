package com.example.couturecorner.data.remote

import com.example.couturecorner.data.model.ConvertResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApiService {
    @GET("convert")
    suspend fun convertCurrency(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("amount") amount: Double,
        @Query("api_key") apiKey: String
    ): Response<ConvertResponse>
}
