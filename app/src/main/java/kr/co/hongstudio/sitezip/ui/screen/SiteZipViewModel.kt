package kr.co.hongstudio.sitezip.ui.screen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.google.firebase.database.*
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.hongstudio.sitezip.R
import kr.co.hongstudio.sitezip.admob.AdMobManager
import kr.co.hongstudio.sitezip.base.livedata.Event
import kr.co.hongstudio.sitezip.base.viewmodel.BaseViewModel
import kr.co.hongstudio.sitezip.data.local.entity.Site
import kr.co.hongstudio.sitezip.data.local.entity.SiteZip
import kr.co.hongstudio.sitezip.firebase.FireBaseDatabaseManager
import kr.co.hongstudio.sitezip.repositories.repository.SiteRepository
import kr.co.hongstudio.sitezip.util.ClipboardUtil
import kr.co.hongstudio.sitezip.util.LogUtil
import kr.co.hongstudio.sitezip.util.ResourceProvider
import kr.co.hongstudio.sitezip.util.UrlParserUtil
import kr.co.hongstudio.sitezip.util.extension.*

class SiteZipViewModel(

    savedStateHandle: SavedStateHandle,
    private val fireBaseDatabaseManager: FireBaseDatabaseManager,
    private val siteRepository: SiteRepository,
    private val clipboardUtil: ClipboardUtil,
    private val urlParserUtil: UrlParserUtil,
    private val resourceProvider: ResourceProvider,
    private val adMobManager: AdMobManager

) : BaseViewModel(), SiteViewHolder.ViewModel {


    object Serializable {
        const val SITE_ZIP: String = "SiteZipViewModel.Serializable.SITE_ZIP"
    }

    companion object {
        const val TAG: String = "SiteZipViewModel"
    }

    /**
     * URL 이동 이벤트.
     */
    private val _intentUrlEvent: MutableLiveData<Event<String>> = MutableLiveData()
    val intentUrlEvent: LiveData<Event<String>> = _intentUrlEvent

    /**
     * 사이트 유형.
     */
    private val _siteZip: MutableLiveData<SiteZip> =
        savedStateHandle.getLiveData(Serializable.SITE_ZIP)
    val siteZip: LiveData<SiteZip> = _siteZip

    /**
     * 사이트 유형.
     */
    private val _searchSiteZip: MutableLiveData<SiteZip> = MutableLiveData()
    val searchSiteZip: LiveData<SiteZip> = _searchSiteZip

    /**
     * 사이트 유형.
     */
    private val _shareLink: MutableLiveData<Event<String>> = MutableLiveData()
    val shareLink: LiveData<Event<String>> = _shareLink

    /**
     * 사이트 없음 표시.
     */
    private val _isShowNotFoundSite: MutableLiveData<Boolean> = MutableLiveData()
    val isShowNotFoundSite: LiveData<Boolean> = _isShowNotFoundSite

    /**
     * 즐겨찾기만 표시 여부.
     */
    private val _isFavoriteMode: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * 검색 텍스트.
     */
    private val _searchText: MutableLiveData<String> = MutableLiveData()


    private var typeRef: DatabaseReference? =
        fireBaseDatabaseManager.database.getReference(
            fireBaseDatabaseManager.rootPath +
                    "/${_siteZip.value?.typeName ?: ""}"
        )

    private val firebaseTypeRefInitListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            Log.d(TAG, "_siteZip.value?.typeName : ${_siteZip.value?.typeName}")
            val tempSiteZip: SiteZip = _siteZip.value?.copy() ?: return
            tempSiteZip.run { this.siteList.clear() }
            showProgress(true)
            getLocalDatabaseSite(
                siteTypeName = _siteZip.value?.typeName ?: "",
                onComplete = {
                    for (siteDataSnapshot: DataSnapshot in dataSnapshot.children) {
                        if (!(siteDataSnapshot.key ?: "").contains(SiteZip.SITE)
                            || siteDataSnapshot.key == null
                        ) {
                            continue
                        }
                        val site: Site? = siteDataSnapshot.getValue(Site::class.java).apply {
                            this?.siteTypeName = _siteZip.value?.typeName ?: ""
                            this?.isUseHttpIcon =
                                siteDataSnapshot.child(Site.IS_USE_HTTP_ICON_VAR_NAME)
                                    .getValue(Boolean::class.java)
                                    ?: true
                            this?.sitePrimaryKey =
                                "${_siteZip.value?.typeName ?: ""}_${siteDataSnapshot.key}"

                            val url: String = urlParserUtil.extractUrlFromText(this?.url ?: "")
                            if (url.isNotEmpty()) {
                                urlParserUtil.getMetadataFromUrl(url)?.also { metaData ->
                                    if (metaData.title.isNotEmpty()) {
                                        this?.title = metaData.title
                                    }
                                    if (metaData.imageUrl.isNotEmpty()) {
                                        if (this?.isUseHttpIcon == true) {
                                            this.iconUrl = metaData.imageUrl
                                        }
                                    }
                                    if (metaData.description.isNotEmpty()) {
                                        this?.description = metaData.description
                                    }
                                }
                            }
                        }
                        for (chooseFavoriteSite: Site in it) {
                            if (chooseFavoriteSite.sitePrimaryKey == site?.sitePrimaryKey) {
                                site.isFavorite = true
                                break
                            }
                        }
                        tempSiteZip.siteList.add(site ?: continue)
                    }
                    Log.d(TAG, "getSite. onDataChange. ${tempSiteZip.siteList.size}")
                    if (tempSiteZip.siteList.size <= 0) {
                        return@getLocalDatabaseSite
                    }
                    _siteZip.postValue = tempSiteZip
                    dismissProgress(true)
                },
                onError = {
                    Log.d(TAG, it.toString())
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
            dismissProgress(true)
        }
    }

    fun getDisplaySiteZip() {
        val tempSiteZip: SiteZip = (_siteZip.value?.copy() ?: return).apply {
            siteList = if (_isFavoriteMode.value == true) {
                this.siteList.filter {
                    it.isFavorite && (it.title.contains(_searchText.value ?: "", ignoreCase = true)
                            || it.url.contains(_searchText.value ?: "", ignoreCase = true))
                }.toMutableList()
            } else {
                this.siteList.filter {
                    it.title.contains(_searchText.value ?: "", ignoreCase = true)
                            || it.url.contains(_searchText.value ?: "", ignoreCase = true)
                }.toMutableList()
            }
            siteList.sortBy {
                it.id
            }
            _isShowNotFoundSite.value = siteZip.value?.siteList?.size ?: 0 > 0 && siteList.size <= 0
        }
        _searchSiteZip.setValueIfNew(tempSiteZip)
    }

    /**
     * 사이트 정보 가져오기.
     */
    fun getSite() = typeRef?.addValueEventListener(firebaseTypeRefInitListener)


    fun setFavoriteMode(isFavorite: Boolean) {
        _isFavoriteMode.value = isFavorite
    }

    fun setSearchText(text: String) {
        _searchText.value = text
    }

    override fun intentUrl(url: String) {
        adMobManager.showInterstitialAd()
        _intentUrlEvent.notify = url
    }

    override fun copyLink(label: String, text: String) {
        LogUtil.d(MainViewModel.TAG, "clipBoard. label : $label / text : $text")
        clipboardUtil.copyText(label, text)
        showToast(resourceProvider.getString(R.string.copy_to_link_message))
    }

    override fun shareLink(text: String) {
        _shareLink.notify = text
    }

    override fun chooseFavorite(site: Site) {
        _siteZip.value = setFavoriteSite(site, true)
        _siteZip.refresh(true)
        compositeDisposable += siteRepository.insert(site = site)
    }

    override fun releaseFavorite(site: Site) {
        _siteZip.value = setFavoriteSite(site, false)
        _siteZip.refresh(true)
        compositeDisposable += siteRepository.delete(
            primaryKey = site.sitePrimaryKey
        )
    }

    private fun setFavoriteSite(site: Site, isFavorite: Boolean): SiteZip? {
        val tempSiteType = _siteZip.value ?: return null
        tempSiteType.siteList[tempSiteType.siteList.indexOfFirst {
            it.sitePrimaryKey == site.sitePrimaryKey
        }] = site.copy(isFavorite = isFavorite)
        return tempSiteType
    }

    /**
     * 로컬 데이베이스내 사이트 정보 가져오기.
     */
    private fun getLocalDatabaseSite(
        siteTypeName: String,
        onComplete: ((allSite: List<Site>) -> Unit),
        onError: (e: Throwable) -> Unit
    ) {
        compositeDisposable += siteRepository.getAllSites(siteTypeName)
            .firstOrError()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribeBy(
                onSuccess = { allSites ->
                    onComplete(allSites)
                },
                onError = {
                    onError(it)
                }
            )
    }

    /**
     * 파이어베이스 typeRef 리스너 제거.
     */
    private fun removeFirebaseListener() {
        typeRef?.removeEventListener(firebaseTypeRefInitListener)
    }

    fun onBind(item: SiteZip) {
        _siteZip setValueIfNew item
        removeFirebaseListener()
        typeRef = null
        typeRef = fireBaseDatabaseManager.database.getReference(
            fireBaseDatabaseManager.rootPath +
                    "/${item.typeName}"
        )
        typeRef?.addValueEventListener(firebaseTypeRefInitListener)
    }

    override fun onCleared() {
        removeFirebaseListener()
        super.onCleared()
    }
}