package kr.co.honga.sitezip.ui.screen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.honga.sitezip.base.livedata.EmptyEvent
import kr.co.honga.sitezip.base.viewmodel.BaseViewModel
import kr.co.honga.sitezip.data.BuildProperty
import kr.co.honga.sitezip.data.local.entity.Site
import kr.co.honga.sitezip.data.local.entity.SiteZip
import kr.co.honga.sitezip.firebase.FireBaseDatabaseUtil
import kr.co.honga.sitezip.repositories.repository.SiteRepository
import kr.co.honga.sitezip.util.UrlParserUtil
import kr.co.honga.sitezip.util.extension.notify

class MainViewModel(

    private val fireBaseDatabaseUtil: FireBaseDatabaseUtil,
    private val siteRepository: SiteRepository,
    private val urlParserUtil: UrlParserUtil,
    private val buildProperty: BuildProperty

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
    val isShowBannerAdmob: LiveData<Boolean> = MutableLiveData(buildProperty.useGoogleAdmob)

    /**
     * 사이트 유형 가져오기.
     */
    fun getSiteTypes() {
        showProgress(true)
        fireBaseDatabaseUtil.database.getReference("${FireBaseDatabaseUtil.SITES_PATH}/${buildProperty.products}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
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
                            onError = { throwable ->
                                throwable.printStackTrace()
                            }
                        )
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(
                        FireBaseDatabaseUtil.TAG,
                        "loadPost:onCancelled",
                        databaseError.toException()
                    )
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
     * 검색 레이아웃 활성화 여부.
     */
    fun setSearchVisibility(isEnable: Boolean) {
        _searchVisibility.value = isEnable
    }

    /**
     * 즐겨찾기만 표시 여부.
     */
    fun setFavoriteMode(isFavoriteMode: Boolean) {
        _isFavoriteMode.value = isFavoriteMode
    }

    /**
     * 기타 메뉴 표시 여부.
     */
    fun setShowMoreMenu(isShow: Boolean) {
        _isShowMoreMenu.value = isShow
    }
}