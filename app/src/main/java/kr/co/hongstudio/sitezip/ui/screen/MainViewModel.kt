package kr.co.hongstudio.sitezip.ui.screen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kr.co.hongstudio.sitezip.base.livedata.EmptyEvent
import kr.co.hongstudio.sitezip.base.viewmodel.BaseViewModel
import kr.co.hongstudio.sitezip.data.BuildProperty
import kr.co.hongstudio.sitezip.data.local.entity.SiteZip
import kr.co.hongstudio.sitezip.firebase.FireBaseDatabaseManager
import kr.co.hongstudio.sitezip.observer.NetworkObserver
import kr.co.hongstudio.sitezip.util.extension.map
import kr.co.hongstudio.sitezip.util.extension.notify

class MainViewModel(

    buildProperty: BuildProperty,
    private val fireBaseDatabaseManager: FireBaseDatabaseManager,
    private val networkObserver: NetworkObserver

) : BaseViewModel() {

    companion object {
        const val TAG: String = "MainViewModel"
    }

    /**
     * 앱 타이틀.
     */
    val appTitle: MutableLiveData<String> =MutableLiveData(buildProperty.appTitleName)

    /**
     * 사이트집 리스트. (No LiveData)
     */
    private val siteZipList: MutableList<SiteZip> = mutableListOf()

    /**
     * 사이트집 라이브데이터.
     */
    private val _siteZips: MutableLiveData<MutableList<SiteZip>> = MutableLiveData()
    val siteZips: LiveData<MutableList<SiteZip>> = _siteZips

    /**
     * 검색 이벤트.
     */
    private val _searchVisibility: MutableLiveData<Boolean> = MutableLiveData(false)
    val searchVisibility: LiveData<Boolean> = _searchVisibility

    /**
     * 기타 메뉴 표시 이벤트.
     */
    private val _isShowMoreMenu: MutableLiveData<Boolean> = MutableLiveData(false)
    val isShowMoreMenu: LiveData<Boolean> = _isShowMoreMenu

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
     * 즐겨찾기만 표시 여부.
     */
    private val _isFavoriteMode: MutableLiveData<Boolean> = MutableLiveData()
    val isFavoriteMode: LiveData<Boolean> = _isFavoriteMode

    /**
     * 애드몹 배너 표시 여부.
     */
    private val _isShowBannerAdMob: MutableLiveData<Boolean> =
        MutableLiveData(buildProperty.useGoogleAdmob)
    val isShowBannerAdmob: LiveData<Boolean> = _isShowBannerAdMob

    /**
     * 음성 검색 시작 이벤트.
     */
    private val _billingRemoveAds: MutableLiveData<EmptyEvent> = MutableLiveData()
    val billingRemoveAds: LiveData<EmptyEvent> = _billingRemoveAds

    /**
     * 뷰페이저 사용자 제스처 사용 여부.
     */
    private val _setViewPagerUserInputEnabled : MutableLiveData<Boolean> = MutableLiveData()
    val setViewPagerUserInputEnabled: LiveData<Boolean> = _setViewPagerUserInputEnabled

    /**
     * 콘텐츠 활성화 여부.
     */
    val isEnableContents: LiveData<Boolean> =
        fireBaseDatabaseManager.isAvailable.map { isAvailable ->
            Log.d(TAG, "isAvailable $isAvailable")
            if (siteZips.value?.size ?: 0 > 0) {
                return@map true
            } else {
                return@map isAvailable
            }
        }

    /**
     * 네트워크 연결 여부.
     */
    val isNetworkAvailable: LiveData<Boolean> = networkObserver.isAvailable

    /**
     * 네트워크 오류 레이아웃 표시 여부.
     */
    private val _isShowNetworkErrorLayout: MutableLiveData<Boolean> = MutableLiveData()
    val isShowNetworkErrorLayout: LiveData<Boolean> = _isShowNetworkErrorLayout

    /**
     * 파이어베이스 루트 ref 리스너.
     */
    private val firebaseRootRefListener: ChildEventListener = object : ChildEventListener {

        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d(TAG, "firebaseRootRefListener. onChildAdded. ${snapshot.key ?: ""}}")
            siteZipList.let { list ->
                if (list.any { it.typeName == snapshot.key ?: "" }) {
                    return
                }
                list.add(snapshot.getValue(SiteZip::class.java).apply {
                    this?.typeName = snapshot.key ?: ""
                    this?.index = snapshot.child(SiteZip.INDEX)
                        .getValue(Int::class.java)
                        ?: 0
                } ?: return)
            }
            _siteZips.value = siteZipList.apply {
                sortBy {
                    it.index
                }
            }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            Log.d(TAG, "firebaseRootRefListener. onChildRemoved. ${snapshot.key ?: ""}}")
            val removeIndex = siteZipList.indexOfFirst {
                it.typeName == snapshot.key
            }
            if(removeIndex < 0) {
                return
            }
            siteZipList.removeAt(removeIndex)
            _siteZips.value = siteZipList.apply {
                sortBy {
                    it.index
                }
            }
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d(TAG, "firebaseRootRefListener. onChildMoved.")
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d(TAG, "firebaseRootRefListener. onChildChanged.")
        }

        override fun onCancelled(error: DatabaseError) {
            Log.d(
                FireBaseDatabaseManager.TAG,
                "loadPost:onCancelled",
                error.toException()
            )
            if (isEnableContents.value == false) {
                Log.d(TAG, "getSiteTypes.ValueEventListener. network available false.")
            }
        }
    }

    /**
     * 사이트 유형 가져오기.
     */
    fun getSiteZips(): ChildEventListener =
        fireBaseDatabaseManager.rootRef.addChildEventListener(firebaseRootRefListener)

    /**
     * 사이트 검색.
     */
    fun searchSites() {
        _isSearchTextChanged.value = !searchText.value.isNullOrEmpty()
    }

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
     * 검색 레이아웃 활성화 여부 설정.
     */
    fun setSearchVisibility(isEnable: Boolean) {
        _searchVisibility.value = isEnable
    }

    /**
     * 즐겨찾기만 표시 여부 설정.
     */
    fun setFavoriteMode(isFavoriteMode: Boolean) {
        _isFavoriteMode.value = isFavoriteMode
    }

    /**
     * 기타 메뉴 표시 여부 설정.
     */
    fun setShowMoreMenu(isShow: Boolean) {
        _isShowMoreMenu.value = isShow
    }

    /**
     * 네트워크 에러 레이아웃 표시 여부 설정.
     */
    fun setShowNetworkErrorLayout(isShow: Boolean) {
        if (isShow && isEnableContents.value == true) {
            return
        }
        _isShowNetworkErrorLayout.value = isShow
    }

    /**
     * 배너 광고 펴시 여부 설정.
     */
    fun setShowBannerAds(isShow: Boolean) {
        if (!isShow && isEnableContents.value == true) {
            return
        }
        _isShowBannerAdMob.value = isShow
    }

    /**
     * 초기 네트워크 레이아웃 표시 여부.
     */
    fun setViewCheckNetwork() {
        if (!networkObserver.isNetworkConnected()) {
            setShowBannerAds(false)
            setShowNetworkErrorLayout(true)
        } else {
            setShowBannerAds(true)
            setShowNetworkErrorLayout(false)
        }
    }

    /**
     * 광고제거 청구 (인앱 결제).
     */
    fun billingRemoveAds() {
        _billingRemoveAds.notify()
    }

    /**
     * 파이어베이스 루트 ref 리스너 제거.
     */
    private fun removeRootRefListener() {
        fireBaseDatabaseManager.removeRootRefListener(firebaseRootRefListener)
    }

    /**
     * 뷰페이저 사용자 제스처 사용 여부.
     */
    fun setViewPagerUserInputEnabled(isEnable: Boolean) {
        _setViewPagerUserInputEnabled.value = isEnable
    }

    override fun onCleared() {
        super.onCleared()
        removeRootRefListener()
    }
}