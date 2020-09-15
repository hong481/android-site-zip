package kr.co.hongstudio.sitezip.ui.screen.place

import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kr.co.hongstudio.sitezip.base.livedata.Event
import kr.co.hongstudio.sitezip.base.viewmodel.BaseViewModel
import kr.co.hongstudio.sitezip.util.LocationUtil
import kr.co.hongstudio.sitezip.util.extension.notify

class PlaceListViewModel(

    private val locationUtil: LocationUtil

) : BaseViewModel() {

    companion object {
        const val TAG: String = "SiteZipViewModel"
    }

    object Serializable {
        const val PLACE: String = "PlaceListViewModel.Serializable.PLACE"
    }

    /**
     * 권한 허용 여부.
     */
    private val _permissionGranted: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val permissionGranted: MutableLiveData<Event<Boolean>> = _permissionGranted

    /**
     * 현재 위치 정보.
     */
    val location: LiveData<Location> = locationUtil.location

    /**
     * 현재 주소 정보.
     */
    private val _geocoder: MutableLiveData<Geocoder> = MutableLiveData()
    val geocoder: MutableLiveData<Geocoder> = _geocoder

    /**
     * 위치 콜백 등록.
     */
    fun registerLocationCallback() {
        locationUtil.registerLocationCallback()
    }

    /**
     * 위치 콜백 해제.
     */
    fun unregisterLocationCallback() {
        locationUtil.unregisterLocationCallback()
    }

    /**
     * 지오코더 설정.
     */
    fun setGeocoder() {

    }

    /**
     * 권한 허용 여부 설정.
     */
    fun setPermissionGranted(isGranted : Boolean) {
        _permissionGranted.notify = isGranted
    }
}