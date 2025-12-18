package com.kholhang.kcclabu.extensions.connectivity

import android.net.ConnectivityManager
import android.net.NetworkCapabilities

val ConnectivityManager.isConnected: Boolean
    get() = isConnectedToWifi || isConnectedToCellular

val ConnectivityManager.isConnectedToWifi: Boolean
    get() {
        // minSdk is 23, so we can use modern API directly
        val capabilities = getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

val ConnectivityManager.isConnectedToCellular: Boolean
    get() {
        // minSdk is 23, so we can use modern API directly
        val capabilities = getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
