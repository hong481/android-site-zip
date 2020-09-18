package kr.co.hongstudio.sitezip.ui.screen.place

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import io.reactivex.rxjava3.kotlin.plusAssign
import kr.co.hongstudio.sitezip.base.livedata.EmptyEvent
import kr.co.hongstudio.sitezip.base.livedata.Event
import kr.co.hongstudio.sitezip.base.model.Model
import kr.co.hongstudio.sitezip.base.viewmodel.BaseViewModel
import kr.co.hongstudio.sitezip.data.local.entity.PlaceZip
import kr.co.hongstudio.sitezip.domain.GetPlacesUseCase
import kr.co.hongstudio.sitezip.util.LocationUtil
import kr.co.hongstudio.sitezip.util.extension.map
import kr.co.hongstudio.sitezip.util.extension.notify
import java.lang.Exception

class PlaceZipViewModel(

    savedStateHandle: SavedStateHandle,
    private val applicationContext: Context,
    private val getPlacesUseCase: GetPlacesUseCase,
    private val locationUtil: LocationUtil

) : BaseViewModel(), PlacesHolder.ViewModel {

    companion object {
        const val TAG: String = "PlaceListViewModel"
    }

    object Serializable {
        const val PLACE_ZIP: String = "PlaceListViewModel.Serializable.PLACE_ZIP"
    }

    /**
     * Place Zip.
     */
    val placeZip: MutableLiveData<PlaceZip> = savedStateHandle.getLiveData(Serializable.PLACE_ZIP)

    /**
     * 권한 허용 여부.
     */
    private val _permissionGranted: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val permissionGranted: MutableLiveData<Event<Boolean>> = _permissionGranted

    /**
     * 준비중 표시.
     */
    val isComingSoon: LiveData<Boolean> = placeZip.map {
        it?.state == Model.FALSE
    }

    /**
     * 현재 위치 정보.
     */
    val location: LiveData<Location> = locationUtil.location

    /**
     * 현재 주소 정보.
     */
    private val _address: MutableLiveData<Address> = MutableLiveData()
    val address: MutableLiveData<Address> = _address

    /**
     * URL 이동 이벤트.
     */
    private val _intentUrlEvent: MutableLiveData<Event<String>> = MutableLiveData()
    val intentUrlEvent: LiveData<Event<String>> = _intentUrlEvent

    /**
     * 검색어.
     */
    val searchText: MutableLiveData<String> = MutableLiveData()

    /**
     * 음성 검색 시작 이벤트.
     */
    private val _playVoiceSearch: MutableLiveData<EmptyEvent> = MutableLiveData()
    val playVoiceSearch: LiveData<EmptyEvent> = _playVoiceSearch

    /**
     * 검색 텍스트 변경 여부.
     */
    private val _isSearchTextChanged: MutableLiveData<Boolean> = MutableLiveData()
    val isSearchTextChanged: LiveData<Boolean> = _isSearchTextChanged

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
     * 초기 장소 정보 가져오기. (위치 기반)
     */
    fun getInitPlaces(query: String) {
        if (placeZip.value?.places?.size ?: return <= 0) {
            getPlaces(query)
            setSearchText(query)
        }
    }

    /**
     * 장소 정보 가져오기.
     */
    fun getPlaces(query: String) {
        compositeDisposable += getPlacesUseCase.request(GetPlacesUseCase.Request(query)) { response ->
            Log.d(TAG, "total: ${response.total} items:${response.items}")
            val tempPlaceZip = placeZip.value?.copy()
            tempPlaceZip?.apply {
                places = response.items.filter {
                    it.category != null && (it.category ?: "").contains(
                        placeZip.value?.defaultQuery ?: ""
                    )
                }.toMutableList()
            }
            placeZip.value = tempPlaceZip
        }
    }

    /**
     * 위치 설정.
     */
    fun setAddress(location: Location) {
        try {
            val geocoder = Geocoder(applicationContext)
            val addresses: List<Address> = geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            )
            if (addresses.isNotEmpty()) {
                _address.value = addresses.first()
            }
        } catch (exception: Exception) {
            Log.d(TAG, exception.toString())
        }
    }

    /**
     * 검색 텍스트 설정.
     */
    fun setSearchText(text: String = "") {
        searchText.value = text
    }

    /**
     * 음성 검색 시작.
     */
    fun playVoiceSearch() {
        _playVoiceSearch.notify()
    }

    /**
     * 바인딩.
     */
    fun onBind(item: PlaceZip) {
        placeZip.value = item
    }

    /**
     * 권한 허용 여부 설정.
     */
    fun setPermissionGranted(isGranted: Boolean) {
        _permissionGranted.notify = isGranted
    }

    /**
     * 장소 페이지로 이동.
     */
    override fun intentPlacePage(placeId: Long?) {
        placeId?.let {
            _intentUrlEvent.notify = "${placeZip.value?.placeUrl}$it"
        }
    }
}