package com.example.couturecorner.Utility

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class NetworkUtils(private val context: Context) {

    fun observeNetworkState(): Flow<Boolean> =
        callbackFlow {
            val connctivityManager = getSystemService(context, ConnectivityManager::class.java)!!
            val networkCallback = object : ConnectivityManager.NetworkCallback() {

                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    trySend(true)
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    Log.d("NetworkUtils", "Network is lost")
                    trySend(false)
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    Log.d("NetworkUtils", "Network is unavailable")
                    trySend(false)
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    Log.d("NetworkUtils", "Network is losing")
                    trySend(false)
                }


            }
            val networkRequest = NetworkRequest.Builder().build()
            try {
                connctivityManager.registerNetworkCallback(networkRequest, networkCallback)
            } catch (e: Exception) {
                Log.e("NetworkUtils", "Error registering network callback: ${e.message}")
                // Handle error (consider retrying or notifying the app)
            }
            awaitClose {
                connctivityManager.unregisterNetworkCallback(networkCallback)
            }
        }

    fun hasNetworkConnection(): Boolean {
        val connectivityManager = getSystemService(context, ConnectivityManager::class.java)!!
        val activeNetwork: Network? = connectivityManager.activeNetwork
        return activeNetwork != null && connectivityManager.getNetworkCapabilities(activeNetwork) != null
    }
}