package kr.co.hongstudio.sitezip.observer

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kr.co.hongstudio.sitezip.util.extension.postValue

class NetworkObserver(

    private val applicationContext: Context

) : ConnectivityManager.NetworkCallback() {

    private val _isAvailable: MutableLiveData<Boolean> = MutableLiveData()
    val isAvailable: LiveData<Boolean> = _isAvailable

    companion object {
        const val TAG: String = "NetworkObserver"
    }

    private val instance: ConnectivityManager by lazy {
        applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val networkRequest: NetworkRequest by lazy {
        NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
    }

    private val connectivityManager: ConnectivityManager by lazy {
        applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    fun register() {
        Log.d(TAG, "enable.")
        this.instance.registerNetworkCallback(networkRequest, this)
    }

    fun unregister() {
        Log.d(TAG, "disable.")
        this.instance.unregisterNetworkCallback(this)
    }

    override fun onAvailable(network: Network) {
        if (isNetworkConnected()) {
            Log.d(TAG, "onAvailable.")
            _isAvailable.postValue = true
        } else {
            _isAvailable.postValue = false
        }
    }

    override fun onLost(network: Network) {
        Log.d(TAG, "onLost.")
        _isAvailable.postValue = false
    }

    fun isNetworkConnected(): Boolean {
        val capabilities: NetworkCapabilities? =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

}