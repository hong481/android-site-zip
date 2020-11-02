package kr.co.hongstudio.sitezip.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*


class LocationUtil(
    applicationContext: Context
) {

    companion object {
        const val UPDATE_INTERVAL_MS: Long = 1000L
        const val FASTEST_UPDATE_INTERVAL_MS: Long = 500L
    }

    /**
     * 현재 위치 정보.
     */
    private val _location: MutableLiveData<Location> = MutableLiveData()
    val location: LiveData<Location> = _location


    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(applicationContext)
    }

    private val locationManager: LocationManager by lazy {
        applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
    }


    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                _location.value = location
            }
        }
    }

    private val locationRequest: LocationRequest = LocationRequest().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        interval = UPDATE_INTERVAL_MS
        fastestInterval = FASTEST_UPDATE_INTERVAL_MS;
    }

    /**
     * 위치 서비스 활성화 확인.
     */
    fun checkLocationServicesStatus(): Boolean {
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        ))
    }

    /**
     * 마지막 위치 가져오기. (콜백)
     */
    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            _location.value = location
        }
    }

    /**
     * 콜백 등록.
     */
    @SuppressLint("MissingPermission")
    fun registerLocationCallback() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }

    /**
     * 콜백 해제.
     */
    fun unregisterLocationCallback() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}