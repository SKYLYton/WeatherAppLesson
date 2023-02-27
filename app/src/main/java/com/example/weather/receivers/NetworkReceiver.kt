package com.example.weather.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.example.weather.Constants

class NetworkReceiver : BroadcastReceiver() {
    var onNetworkStateListener: ((Boolean) -> Unit)? = null


    override fun onReceive(context: Context, intent: Intent) {
        if (onNetworkStateListener == null) {
            return
        }
        val online = isOnline(context)
        val actionOfIntent: String? = intent.getAction()
        if (actionOfIntent == Constants.CONNECTIVITY_ACTION) {
            onNetworkStateListener?.invoke(online)
        }
    }

    fun isOnline(context: Context): Boolean {
        val connMgr: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connMgr.getActiveNetworkInfo()
        return networkInfo != null && networkInfo.isConnected()
    }
}