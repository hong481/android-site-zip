package kr.co.hongstudio.sitezip.ui.screen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.hongstudio.sitezip.base.livedata.EmptyEvent
import kr.co.hongstudio.sitezip.base.viewmodel.BaseViewModel
import kr.co.hongstudio.sitezip.data.BuildProperty
import kr.co.hongstudio.sitezip.data.local.entity.Site
import kr.co.hongstudio.sitezip.data.local.entity.SiteZip
import kr.co.hongstudio.sitezip.firebase.FireBaseDatabaseManager
import kr.co.hongstudio.sitezip.observer.NetworkObserver
import kr.co.hongstudio.sitezip.repositories.repository.SiteRepository
import kr.co.hongstudio.sitezip.util.UrlParserUtil
import kr.co.hongstudio.sitezip.util.extension.map
import kr.co.hongstudio.sitezip.util.extension.notify

class MainViewModel(

    private val fireBaseDatabaseManager: FireBaseDatabaseManager,
    private val siteRepository: SiteRepository,
    private val urlParserUtil: UrlParserUtil,
    private val buildProperty: BuildProperty,
    private val networkObserver: NetworkObserver

) : BaseViewModel() {

    companion object {
        const val TAG: String = "MainViewModel"
    }

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
    private val _isShowBannerAdMob :MutableLiveData<Boolean> = MutableLiveData(buildProperty.useGoogleAdmob)
    val isShowBannerAdmob: LiveData<Boolean> = _isShowBannerAdMob

    /**
     * 음성 검색 시작 이벤트.
     */
    private val _billingRemoveAds: MutableLiveData<EmptyEvent> = MutableLiveData()
    val billingRemoveAds: LiveData<EmptyEvent> = _billingRemoveAds

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
     * 사이트 유형 가져오기.
     */
    fun getSiteTypes() {
        fireBaseDatabaseManager.database.getReference("${FireBaseDatabaseManager.SITES_PATH}/${buildProperty.products}")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    showProgress(true)
                    Log.d(TAG, dataSnapshot.childrenCount.toString())
                    siteRepository.getAllSites()
                        .firstOrError()
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribeBy(
                            onSuccess = { allSites ->
                                val siteZips: ArrayList<SiteZip> = arrayListOf()
                                for (typeSnapshot: DataSnapshot in dataSnapshot.children) {
                                    val siteType: SiteZip? =
                                        typeSnapshot.getValue(SiteZip::class.java).apply {
                                            this?.typeName = typeSnapshot.key ?: ""
                                            typeSnapshot.children.forEach { dataSnapshot ->
                                                val site: Site? =
                                                    dataSnapshot.getValue(Site::class.java)
                                                        .apply {
                                                            this?.isUseHttpIcon =
                                                                dataSnapshot.child(Site.IS_USE_HTTP_ICON_VAR_NAME)
                                                                    .getValue(Boolean::class.java)
                                                                    ?: true
                                                            this?.sitePrimaryKey =
                                                                "${typeSnapshot.key}_${dataSnapshot.key}"

                                                            val url =
                                                                urlParserUtil.extractUrlFromText(
                                                                    this?.url ?: ""
                                                                )
                                                            if (url.isNotEmpty()) {
                                                                val metadata =
                                                                    urlParserUtil.getMetadataFromUrl(
                                                                        url
                                                                    )
                                                                if (metadata != null) {
                                                                    if (metadata.title.isNotEmpty()) {
                                                                        this?.title =
                                                                            metadata.title
                                                                    }
                                                                    if (metadata.imageUrl.isNotEmpty()) {
                                                                        if (this?.isUseHttpIcon == true) {
                                                                            this.iconUrl =
                                                                                metadata.imageUrl
                                                                        }
                                                                    }
                                                                    if (metadata.description.isNotEmpty()) {
                                                                        this?.description =
                                                                            metadata.description
                                                                    }
                                                                }
                                                            }
                                                        }
                                                for (chooseFavoriteSite: Site in allSites) {
                                                    if (chooseFavoriteSite.sitePrimaryKey == site?.sitePrimaryKey) {
                                                        site.isFavorite = true
                                                        break
                                                    }
                                                }
                                                Log.d(
                                                    TAG,
                                                    "key : ${dataSnapshot.key} / site.isFavorite: ${site?.isFavorite}"
                                                )
                                                this?.siteList?.add(site ?: return@apply)
                                            }
                                        }
                                    if (siteType != null) {
                                        siteZips.add(siteType)
                                    }
                                }
                                _siteZips.postValue(siteZips)
                                dismissProgress(true)
                            },
                            onError = { exception ->
                                Log.d(TAG, exception.toString())
                                dismissProgress(true)
                            }
                        )
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d(
                        FireBaseDatabaseManager.TAG,
                        "loadPost:onCancelled",
                        databaseError.toException()
                    )
                    if (isEnableContents.value == false) {
                        Log.d(TAG, "getSiteTypes.ValueEventListener. network available false.")
                    }
                    dismissProgress(true)
                }
            })
    }


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
        if (!isShow && isEnableContents.value == true) {
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
    fun setViewCheckNetwork(){
        if(!networkObserver.isNetworkConnected()) {
            setShowBannerAds(false)
            setShowNetworkErrorLayout(true)
        }
    }

    /**
     * 광고제거 청구 (인앱 결제).
     */
    fun billingRemoveAds() {
        _billingRemoveAds.notify()
    }
}