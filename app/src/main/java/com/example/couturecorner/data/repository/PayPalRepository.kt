package com.example.couturecorner.data.repository


import android.util.Base64
import android.util.Log
import com.example.couturecorner.data.model.Amount
import com.example.couturecorner.data.model.CaptureRequest
import com.example.couturecorner.data.model.OrderRequest
import com.example.couturecorner.data.model.OrderResponse
import com.example.couturecorner.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PayPalRepository(
    private val clientID: String,
    private val secretID: String
) {

    private var accessToken = ""

    // Function to fetch PayPal Access Token using Retrofit
    suspend fun fetchAccessToken(): String {
        val authString = "$clientID:$secretID"
        val encodedAuthString = "Basic " + Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP)

        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.fetchAccessToken(encodedAuthString)
                accessToken = response.access_token
                Log.i("PayPal", "Access Token: $accessToken")

                accessToken
            } catch (e: Exception) {
                Log.e("PayPal", "Error fetching token: ${e.localizedMessage}")
                throw e // Rethrow exception for the caller to handle
            }
        }
    }

    // Function to create an order
    suspend fun createOrder(orderRequest: OrderRequest): OrderResponse {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.createOrder("Bearer $accessToken", orderRequest.purchase_units[0].reference_id, orderRequest)
                val orderId = response.id
                Log.d("PayPal", "Order Created with ID: $orderId")
                response
            } catch (e: Exception) {
                Log.e("PayPal", "Order creation error: ${e.localizedMessage}")
                throw e // Rethrow exception for the caller to handle
            }
        }
    }

    // Function to capture an order
    suspend fun captureOrder(orderId: String, amount: String, token: String, payerId: String): String {
        val captureRequest = CaptureRequest(
            amount = Amount(
                currency_code = "USD",
                value = amount
            )
        )

        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.captureOrder("Bearer $token", orderId, captureRequest)
                val capturedOrderId = response.id
                Log.i("PayPal", "Order Captured! ID: $capturedOrderId")
                capturedOrderId
            } catch (e: Exception) {
                Log.e("PayPal", "Capture order error: ${e.localizedMessage}")

                throw e // Rethrow exception for the caller to handle
            }
        }
    }
}
