package kr.co.hongstudio.sitezip.ui.screen.place

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxjava3.kotlin.plusAssign
import kr.co.hongstudio.sitezip.base.livedata.EmptyEvent
import kr.co.hongstudio.sitezip.base.livedata.Event
import kr.co.hongstudio.sitezip.base.model.Model
import kr.co.hongstudio.sitezip.base.viewmodel.BaseViewModel
import kr.co.hongstudio.sitezip.data.local.entity.Place
import kr.co.hongstudio.sitezip.data.local.entity.PlaceZip
import kr.co.hongstudio.sitezip.domain.GetPlacesUseCase
import kr.co.hongstudio.sitezip.util.LocationUtil
import kr.co.hongstudio.sitezip.util.extension.map
import kr.co.hongstudio.sitezip.util.extension.notify

class PlaceZipViewModel(

    val locationUtil: LocationUtil,
    private val applicationContext: Context,
    private val getPlacesUseCase: GetPlacesUseCase


) : BaseViewModel(), PlacesHolder.ViewModel {

    companion object {
        const val TAG: String = "PlaceListViewModel"
    }

    /**
     * Place Zip.
     */
    private val _placeZip: MutableLiveData<PlaceZip> = MutableLiveData()
    val placeZip: LiveData<PlaceZip> = _placeZip

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
     * 마커 선택 이벤트.
     */
    private val _selectMapMarkerEvent: MutableLiveData<Event<String>> = MutableLiveData()
    val selectMapMarker: LiveData<Event<String>> = _selectMapMarkerEvent

    /**
     * 검색어.
     */
    val searchText: MutableLiveData<String> = MutableLiveData()

    /**
     * 검색 텍스트 커서 이동.
     */
    private val _searchTextSelection: MutableLiveData<EmptyEvent> = MutableLiveData()
    val searchTextSelection: LiveData<EmptyEvent> = _searchTextSelection

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
            setSearchText(query, true)
        }
    }

    /**
     * 장소 정보 가져오기.
     */
    fun getPlaces(query: String) {
        compositeDisposable += getPlacesUseCase.request(
            GetPlacesUseCase.Request(
                placeApi = placeZip.value?.placeApi ?: "",
                apiKey = placeZip.value?.apiKey ?: "",
                query = query,
                x = location.value?.latitude.toString(),
                y = location.value?.longitude.toString()
            )
        ) { response ->
            Log.d(TAG, "items:${response.items}")
            val tempPlaceZip = placeZip.value?.copy()
            tempPlaceZip?.apply {
                places = response.items.filter {
                    it.category_name != null && (it.category_name ?: "").contains(
                        placeZip.value?.defaultQuery ?: ""
                    )
                }.toMutableList()
            }
            _placeZip.value = tempPlaceZip
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
            if (addresses.isNotEmpty() && _address.value == null) {
                _address.value = addresses.first()
            }
        } catch (exception: Exception) {
            Log.d(TAG, exception.toString())
        }
    }

    /**
     * 사이트 검색 텍스트 존재 여부.
     */
    fun searchSites() {
        _isSearchTextChanged.value = !searchText.value.isNullOrEmpty()
    }

    /**
     * 검색 텍스트 설정.
     */
    fun setSearchText(text: String = "", setSelection: Boolean) {
        searchText.value = text
        if (setSelection) {
            setSearchTextSelection()
        }
    }

    /**
     * 검색 텍스트 커서이동 이벤트.
     */
    fun setSearchTextSelection() {
        _searchTextSelection.notify()
    }

    /**
     * 음성 검색 시작.
     */
    fun playVoiceSearch() {
        _playVoiceSearch.notify()
    }

    /**
     * 장소 오브젝트 찾기.
     */
    fun findPlace(placeName: String): Place? = placeZip.value?.places?.findLast {
        it.place_name == placeName
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
    override fun intentPlacePage(placeUrl: String?) {
        placeUrl?.let {
            _intentUrlEvent.notify = it
        }
    }

    override fun selectMapMarker(placeName: String?) {
        placeName?.let {
            _selectMapMarkerEvent.notify = it
        }
    }

    /**
     * 바인드
     */
    fun onBind(placeZip: PlaceZip) {
        _placeZip.value = placeZip
    }
}