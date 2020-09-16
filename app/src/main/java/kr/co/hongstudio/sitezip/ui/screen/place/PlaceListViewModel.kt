package kr.co.hongstudio.sitezip.ui.screen.place

import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import kr.co.hongstudio.sitezip.base.livedata.Event
import kr.co.hongstudio.sitezip.base.model.Model
import kr.co.hongstudio.sitezip.base.viewmodel.BaseViewModel
import kr.co.hongstudio.sitezip.data.local.entity.Place
import kr.co.hongstudio.sitezip.util.LocationUtil
import kr.co.hongstudio.sitezip.util.extension.map
import kr.co.hongstudio.sitezip.util.extension.notify

class PlaceListViewModel(

    savedStateHandle: SavedStateHandle,
    private val locationUtil: LocationUtil

) : BaseViewModel() {

    companion object {
        const val TAG: String = "PlaceListViewModel"
    }

    object Serializable {
        const val PLACE: String = "PlaceListViewModel.Serializable.PLACE"
    }

    /**
     * Place.
     */
    val place: MutableLiveData<Place> = savedStateHandle.getLiveData(Serializable.PLACE)

    /**
     * 권한 허용 여부.
     */
    private val _permissionGranted: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val permissionGranted: MutableLiveData<Event<Boolean>> = _permissionGranted

    /**
     * 준비중 표시.
     */
    val isComingSoon: LiveData<Boolean> = place.map {
        it?.state == Model.FALSE
    }

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
     * 바인딩.
     */
    fun onBind(item: Place) {
        place.value = item
    }

    /**
     * 권한 허용 여부 설정.
     */
    fun setPermissionGranted(isGranted : Boolean) {
        _permissionGranted.notify = isGranted
    }
}