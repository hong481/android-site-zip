package kr.co.hongstudio.sitezip.ui.screen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kr.co.hongstudio.sitezip.admob.AdMobManager
import kr.co.hongstudio.sitezip.base.livedata.EmptyEvent
import kr.co.hongstudio.sitezip.base.model.Model
import kr.co.hongstudio.sitezip.base.viewmodel.BaseViewModel
import kr.co.hongstudio.sitezip.data.BuildProperty
import kr.co.hongstudio.sitezip.data.local.entity.PlaceZip
import kr.co.hongstudio.sitezip.data.local.entity.SiteZip
import kr.co.hongstudio.sitezip.data.local.preference.AdMobPreference
import kr.co.hongstudio.sitezip.data.local.preference.BillingPreference
import kr.co.hongstudio.sitezip.firebase.FireBaseDatabaseManager
import kr.co.hongstudio.sitezip.observer.NetworkObserver
import kr.co.hongstudio.sitezip.util.extension.map
import kr.co.hongstudio.sitezip.util.extension.notify
import kr.co.hongstudio.sitezip.util.extension.postValue

class MainViewModel(

    private val buildProperty: BuildProperty,
    private val billingPref: BillingPreference,
    private val adMobPref: AdMobPreference,
    private val fireBaseDatabaseManager: FireBaseDatabaseManager,
    private val adMobManager: AdMobManager,
    private val networkObserver: NetworkObserver

) : BaseViewModel() {

    companion object {
        const val TAG: String = "MainViewModel"
    }

    /**
     * SiteZip zip 리스트. (No LiveData)
     */
    val siteZipList: MutableList<SiteZip> = mutableListOf()

    /**
     * SiteZip zip 라이브데이터.
     */
    private val _siteZips: MutableLiveData<MutableList<SiteZip>> = MutableLiveData()
    val siteZips: LiveData<MutableList<SiteZip>> = _siteZips

    /**
     * Place zip 라이브데이터.
     */
    private val _placeZip: MutableLiveData<PlaceZip> = MutableLiveData()
    val placeZip: LiveData<PlaceZip> = _placeZip

    /**
     * Model zip 개수.
     */
    private val _zipSize: MutableLiveData<Int> = MutableLiveData()
    val zipSize: LiveData<Int> = _zipSize

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

    private val _isVisibleSiteScreen: MutableLiveData<Boolean> = MutableLiveData(true)
    val isVisibleSiteScreen: LiveData<Boolean> = _isVisibleSiteScreen

    private val _isVisiblePlaceScreen: MutableLiveData<Boolean> = MutableLiveData(false)
    val isVisiblePlaceScreen: LiveData<Boolean> = _isVisiblePlaceScreen

    private val _replaceSiteScreen: MutableLiveData<EmptyEvent> = MutableLiveData()
    val replaceSiteScreen: LiveData<EmptyEvent> = _replaceSiteScreen

    private val _replacePlaceScreen: MutableLiveData<EmptyEvent> = MutableLiveData()
    val replacePlaceScreen: LiveData<EmptyEvent> = _replacePlaceScreen

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
     * 검색 버튼 표시 여부.
     */
    private val _isSearchButtonVisible: MutableLiveData<Boolean> = MutableLiveData()
    val isSearchButtonVisible: LiveData<Boolean> = _isSearchButtonVisible

    /**
     * 줄겨찾기 버튼 표시 여부.
     */
    private val _isFavoriteButtonVisible: MutableLiveData<Boolean> = MutableLiveData()
    val isFavoriteButtonVisible: LiveData<Boolean> = _isFavoriteButtonVisible

    /**
     * 애드몹 사용 여부.
     */
    val isUseAdmob: MutableLiveData<Boolean> =
        MutableLiveData(buildProperty.useGoogleAdmob && !billingPref.removeAds)

    /**
     * 애드몹 배너 표시 여부.
     */
    private val _isShowBannerAdMob: MutableLiveData<Boolean> =
        MutableLiveData(!billingPref.removeAds)
    val isShowBannerAdmob: LiveData<Boolean> = _isShowBannerAdMob

    /**
     * 광고제거 결제 이벤트.
     */
    private val _billingRemoveAds: MutableLiveData<EmptyEvent> = MutableLiveData()
    val billingRemoveAds: LiveData<EmptyEvent> = _billingRemoveAds

    /**
     * 서포트 결제 이벤트.
     */
    private val _billingSponsor: MutableLiveData<EmptyEvent> = MutableLiveData()
    val billingSponsor: LiveData<EmptyEvent> = _billingSponsor

    /**
     * 뷰페이저 사용자 제스처 사용 여부.
     */
    private val _setViewPagerUserInputEnabled: MutableLiveData<Boolean> = MutableLiveData()
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
     * 진행 다이어로그 표시 체크.
     */
    private val _checkVisibleProgress: MutableLiveData<Boolean> = MutableLiveData()
    val checkVisibleProgress: LiveData<Boolean> = _checkVisibleProgress

    /**
     * 앱 리뷰 다이어로그 해제.
     */
    private val _disposeAppiraterDialogEvent: MutableLiveData<EmptyEvent> = MutableLiveData()
    val disposeAppiraterDialogEvent: LiveData<EmptyEvent> = _disposeAppiraterDialogEvent

    /**
     * 파이어베이스 루트 ref 리스너.
     */
    private val rootRefListener: ChildEventListener = object : ChildEventListener {

        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d(TAG, "firebaseRootRefListener. onChildAdded. ${snapshot.key ?: ""}")
            if (snapshot.child(Model.TYPE).getValue(String::class.java).equals(PlaceZip.PLACE)) {
                val placeZip: PlaceZip =
                    (snapshot.getValue(PlaceZip::class.java) as PlaceZip).apply {
                        this.tabName = snapshot.key ?: ""
                        this.defaultQuery =
                            snapshot.child(PlaceZip.DEFAULT_QUERY).getValue(String::class.java)
                                ?: ""
                        this.index = snapshot.child(Model.INDEX).getValue(Int::class.java) ?: 0
                        this.state = snapshot.child(Model.STATE).getValue(Int::class.java) ?: 0
                    }
                _placeZip.value = placeZip
            } else {
                siteZipList.let { list ->
                    if (list.any {
                            it.tabName == snapshot.key ?: ""
                        }) {
                        return
                    }
                    list.add((snapshot.getValue(SiteZip::class.java) as SiteZip).apply {
                        this.tabName = snapshot.key ?: ""
                        this.index = snapshot.child(Model.INDEX).getValue(Int::class.java) ?: 0
                        this.state = snapshot.child(Model.STATE).getValue(Int::class.java) ?: 0
                    })
                }
                _siteZips.value = siteZipList.apply {
                    sortBy {
                        it.index
                    }
                }
            }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            Log.d(TAG, "firebaseRootRefListener. onChildRemoved. ${snapshot.key ?: ""}")

            if (snapshot.child(Model.TYPE).getValue(String::class.java)
                    .equals(PlaceZip.PLACE)
            ) {
                _placeZip.value = null
            } else {
                val removeIndex = siteZipList.indexOfFirst {
                    it.tabName == snapshot.key
                }
                if (removeIndex < 0) {
                    return
                }
                siteZipList.removeAt(removeIndex)

                _siteZips.value = siteZipList.apply {
                    sortBy {
                        it.index
                    }
                }
            }
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d(TAG, "firebaseRootRefListener. onChildMoved.")
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            val primaryKey = "${snapshot.key}"
            if (snapshot.child(Model.TYPE).getValue(String::class.java).equals(PlaceZip.PLACE)) {
                val placeZip: PlaceZip =
                    (snapshot.getValue(PlaceZip::class.java) as PlaceZip).apply {
                        this.tabName = snapshot.key ?: ""
                        this.defaultQuery =
                            snapshot.child(PlaceZip.DEFAULT_QUERY).getValue(String::class.java)
                                ?: ""
                        this.index = snapshot.child(Model.INDEX).getValue(Int::class.java) ?: 0
                        this.state = snapshot.child(Model.STATE).getValue(Int::class.java) ?: 0
                    }
                _placeZip.value = placeZip
            } else {
                var findSiteZip: SiteZip? = null
                for (siteZip: SiteZip in siteZipList) {
                    if (siteZip.tabName == primaryKey) {
                        findSiteZip = siteZip
                        break
                    }
                }
                val changeIndex = siteZipList.indexOf(findSiteZip)
                if (changeIndex < 0) {
                    return
                }
                siteZipList[changeIndex] =
                    ((snapshot.getValue(SiteZip::class.java) as SiteZip).apply {
                        this.tabName = snapshot.key ?: ""
                        this.index = snapshot.child(Model.INDEX).getValue(Int::class.java) ?: 0
                        this.state = snapshot.child(Model.STATE).getValue(Int::class.java) ?: 0
                    })
                _siteZips.value = siteZipList.apply {
                    sortBy {
                        it.index
                    }
                }
            }
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
     * 파이어베이스 탭 개수 리스너.
     */
    private val siteZipSizeRefListener: ValueEventListener = object : ValueEventListener {

        override fun onDataChange(snapshot: DataSnapshot) {
            val count: Int = snapshot.children.filter {
                it.child(Model.TYPE).getValue(String::class.java) == SiteZip.SITE
            }.count()
            _zipSize.value = count
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
     * Model Zip 사이즈 리스너 등록.
     */
    fun registerZipSizeListener() {
        fireBaseDatabaseManager.rootRef.addValueEventListener(siteZipSizeRefListener)
    }

    /**
     * Model Zip 리스트 리스너 등록.
     */
    fun registerZipsListener() {
        fireBaseDatabaseManager.rootRef.addChildEventListener(rootRefListener)
    }

    fun replaceSiteScreen() = _replaceSiteScreen.notify()

    fun replacePlaceScreen() = _replacePlaceScreen.notify()

    fun setVisibleSiteScreen(isVisible: Boolean) {
        _isVisibleSiteScreen.value = isVisible
    }

    fun setVisiblePlaceScreen(isVisible: Boolean) {
        _isVisiblePlaceScreen.value = isVisible
    }

    /**
     * 사이트 검색 텍스트 존재 여부.
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
        if ((!isShow && isEnableContents.value == true && isUseAdmob.value == true)) {
            return
        }
        _isShowBannerAdMob.value = isShow
    }

    /**
     * 초기 네트워크 레이아웃 표시 여부.
     */
    fun setViewCheckNetwork() = if (!networkObserver.isNetworkConnected()) {
        setShowBannerAds(false)
        setShowNetworkErrorLayout(true)
    } else {
        setShowBannerAds(isUseAdmob.value ?: false)
        setShowNetworkErrorLayout(false)
    }

    /**
     * 검색 버튼 표시 설정.
     */
    fun setSearchButtonVisible(isVisible: Boolean) {
        _isSearchButtonVisible.value = isVisible
    }

    /**
     * 즐겨 찾기 버튼 표시 설정.
     */
    fun setFavoriteButtonVisible(isVisible: Boolean) {
        _isFavoriteButtonVisible.value = isVisible
    }

    /**
     * 광고제거 청구 (인앱 결제).
     */
    fun billingRemoveAds() = _billingRemoveAds.notify()

    /**
     * 스폰서 청구 (인앱 결제).
     */
    fun billingSponsor() = _billingSponsor.notify()

    /**
     * 파이어베이스 Zip Site 리스너 제거.
     */
    private fun unregisterSiteZipSizeRefListener() {
        fireBaseDatabaseManager.removeRootRefListener(siteZipSizeRefListener)
    }

    /**
     * 파이어베이스 Root ref 리스너 제거.
     */
    private fun unregisterRootRefListener() {
        fireBaseDatabaseManager.removeRootRefListener(rootRefListener)
    }

    /**
     * 뷰페이저 사용자 제스처 사용 여부.
     */
    fun setViewPagerUserInputEnabled(isEnable: Boolean) {
        _setViewPagerUserInputEnabled.value = isEnable
    }

    /**
     * 애드몹 사용 여부 설정
     */
    fun setUseAdmob() {
        isUseAdmob.value = buildProperty.useGoogleAdmob && !billingPref.removeAds
    }

    /**
     * 전면광고 노출 여부 체크.
     */
    fun checkShowInterstitialAd() {
        if (adMobPref.showInterstitialAdCount >= buildProperty.interstitialAdmobTriggerValue) {
            adMobPref.showInterstitialAdCount = 0
            adMobManager.showInterstitialAd()
        } else {
            adMobPref.showInterstitialAdCount += 1
        }
    }

    /**
     * 진행 다이어로그 표시 체크 설정.
     */
    fun setCheckVisibleProgress(isVisible: Boolean) {
        _checkVisibleProgress.postValue = isVisible
    }

    /**
     * 앱 리뷰 다이어로그 해제.
     */
    fun disposeAppiraterDialog() = _disposeAppiraterDialogEvent.notify()

    override fun onCleared() {
        super.onCleared()
        unregisterSiteZipSizeRefListener()
        unregisterRootRefListener()
    }
}
