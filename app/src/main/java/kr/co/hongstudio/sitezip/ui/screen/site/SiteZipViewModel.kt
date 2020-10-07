package kr.co.hongstudio.sitezip.ui.screen.site

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.google.firebase.database.*
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.hongstudio.sitezip.R
import kr.co.hongstudio.sitezip.base.livedata.EmptyEvent
import kr.co.hongstudio.sitezip.base.livedata.Event
import kr.co.hongstudio.sitezip.base.model.Model
import kr.co.hongstudio.sitezip.base.viewmodel.BaseViewModel
import kr.co.hongstudio.sitezip.data.local.entity.Site
import kr.co.hongstudio.sitezip.data.local.entity.SiteZip
import kr.co.hongstudio.sitezip.firebase.FireBaseDatabaseManager
import kr.co.hongstudio.sitezip.repositories.repository.SiteRepository
import kr.co.hongstudio.sitezip.ui.screen.MainViewModel
import kr.co.hongstudio.sitezip.util.*
import kr.co.hongstudio.sitezip.util.extension.*

class SiteZipViewModel(

    savedStateHandle: SavedStateHandle,
    private val fireBaseDatabaseManager: FireBaseDatabaseManager,
    private val siteRepository: SiteRepository,
    private val clipboardUtil: ClipboardUtil,
    private val urlParserUtil: UrlParserUtil,
    private val resourceProvider: ResourceProvider

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
     * 사이트 Zip.
     */
    var siteZip: SiteZip = savedStateHandle.get(Serializable.SITE_ZIP) ?: SiteZip()

    /**
     * 사이트 Zip.
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
     * 준비중 표시.
     */
    val isComingSoon: LiveData<Boolean> = searchSiteZip.map {
        it?.state == Model.FALSE
    }

    /**
     * 즐겨찾기만 표시 여부.
     */
    private val _isFavoriteMode: MutableLiveData<Boolean> = MutableLiveData()
    val isFavoriteMode: MutableLiveData<Boolean> = _isFavoriteMode

    /**
     * 검색 텍스트.
     */
    private val _searchText: MutableLiveData<String> = MutableLiveData()

    /**
     * 스크롤 뷰를 최상단으로 이동.
     */
    private val _scrollToPositionTop: MutableLiveData<EmptyEvent> = MutableLiveData()
    val scrollToPositionTop: LiveData<EmptyEvent> = _scrollToPositionTop

    /**
     * 진행 다이어로그 표시 체크.
     */
    private val _checkVisibleProgress: MutableLiveData<Boolean> = MutableLiveData()
    val checkVisibleProgress: LiveData<Boolean> = _checkVisibleProgress

    /**
     * 탭 단위 파이어베이스 레퍼런스.
     */
    private var tabRef: DatabaseReference? = makeTabRef(siteZip)

    /**
     * 탭 단위 파이어베이스 레퍼런스 생성.
     */
    private fun makeTabRef(siteZip: SiteZip): DatabaseReference =
        fireBaseDatabaseManager.database.getReference(
            fireBaseDatabaseManager.rootPath +
                    "/${siteZip.tabName}" +
                    "/${FireBaseDatabaseManager.SITES_PATH}"
        )

    /**
     * 탭 단위 파이어베이스 레퍼런스 리스너.
     */
    @SuppressLint("RestrictedApi")
    private val firebaseTabRefListener: ChildEventListener = object : ChildEventListener {

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d(
                TAG,
                "firebaseTabRefListener. onChildMoved. ${snapshot.key} / ${tabRef?.path.toString()}"
            )
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d(
                TAG,
                "firebaseTabRefListener. onChildChanged. ${snapshot.key} / ${tabRef?.path.toString()}"
            )
            if (!(snapshot.key ?: "").contains(SiteZip.SITE) || snapshot.key == null) {
                return
            }
            val primaryKey = "${siteZip.tabName}_${snapshot.key}"
            val changeIndex = siteZip.siteList.indexOfFirst {
                it.sitePrimaryKey == primaryKey
            }
            if (changeIndex < 0) {
                return
            }
            siteZip.siteList[changeIndex] = snapshot.getValue(Site::class.java).apply {
                this?.siteTypeName = siteZip.tabName
                this?.isUseHttpIcon =
                    snapshot.child(Site.IS_USE_HTTP_ICON_VAR_NAME).getValue(Boolean::class.java)
                        ?: true
                this?.sitePrimaryKey = primaryKey
                val url: String = urlParserUtil.extractUrlFromText(this?.url ?: "")
                if (url.isNotEmpty()) {
                    urlParserUtil.getMetadataFromUrl(url)?.also { metaData ->
                        if (metaData.title.isNotEmpty()) {
                            this?.title = metaData.title
                        }
                        if (metaData.description.isNotEmpty()) {
                            this?.description = metaData.description
                        }
                        if (metaData.imageUrl.isNotEmpty() && this?.isUseHttpIcon == true) {
                            this.iconUrl = metaData.imageUrl
                        }
                    }
                }
            } ?: return
            _searchSiteZip.postValue = siteZip.apply {
                siteList.sortBy {
                    it.id
                }
            }
        }

        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d(
                TAG,
                "firebaseTabRefListener. onChildAdded. ${snapshot.key} / ${tabRef?.path.toString()}"
            )
            if (!(snapshot.key ?: "").contains(SiteZip.SITE) || snapshot.key == null) {
                return
            }
            val primaryKey = "${siteZip.tabName}_${snapshot.key}"
            showProgress(true)
            setCheckVisibleProgress(true)
            checkLocalFavoriteSite(
                primaryKey = primaryKey,
                onComplete = { isFavorite ->
                    siteZip.siteList.let { list ->
                        list.add(snapshot.getValue(Site::class.java).apply {
                            this?.siteTypeName = siteZip.tabName
                            this?.isUseHttpIcon = snapshot.child(Site.IS_USE_HTTP_ICON_VAR_NAME)
                                .getValue(Boolean::class.java) ?: true
                            this?.sitePrimaryKey = primaryKey
                            val url: String = urlParserUtil.extractUrlFromText(this?.url ?: "")
                            if (url.isNotEmpty()) {
                                urlParserUtil.getMetadataFromUrl(url)?.also { metaData ->
                                    this?.isFavorite = isFavorite
                                    if (metaData.title.isNotEmpty()) {
                                        this?.title = metaData.title
                                    }
                                    if (metaData.description.isNotEmpty()) {
                                        this?.description = metaData.description
                                    }
                                    if (metaData.imageUrl.isNotEmpty() && this?.isUseHttpIcon == true) {
                                        this.iconUrl = metaData.imageUrl
                                    }
                                }
                            }
                        } ?: return@let)
                    }
                    _searchSiteZip.postValue = siteZip.apply {
                        siteList.sortBy {
                            it.id
                        }
                    }
                    _scrollToPositionTop.postNotify()
                    dismissProgress(true)
                    setCheckVisibleProgress(false)
                },
                onError = {
                    Log.d(TAG, it.toString())
                    dismissProgress(true)
                    setCheckVisibleProgress(false)
                }
            )
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            Log.d(
                TAG,
                "firebaseTabRefListener. onChildRemoved. ${snapshot.key} / ${tabRef?.path.toString()}"
            )
            val primaryKey = "${siteZip.tabName}_${snapshot.key}"
            val removeIndex = siteZip.siteList.indexOfFirst {
                it.sitePrimaryKey == primaryKey
            }
            if (removeIndex < 0) {
                return
            }
            siteZip.siteList.removeAt(removeIndex)
            _searchSiteZip.postValue = siteZip.apply {
                siteList.sortBy {
                    it.id
                }
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.d(TAG, "loadPost:onCancelled", databaseError.toException())
            dismissProgress(true)
        }
    }

    /**
     * 탭 단위 뷰 표시. (리사이클러 뷰)
     */
    fun displayTabViews() = try {
        if (siteZip.siteList.size > 0) {
            val tempSiteZip: SiteZip = siteZip.copy().apply {
                siteList = if (_isFavoriteMode.value == true) {
                    this.siteList.filter {
                        it.isFavorite && (it.title.contains(
                            _searchText.value ?: "",
                            ignoreCase = true
                        )
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
            }
            _isShowNotFoundSite.value = tempSiteZip.siteList.size <= 0
            _searchSiteZip.setValueIfNew(tempSiteZip)
        } else {
            null
        }
    } catch (e: ConcurrentModificationException) {
        Log.w(TAG, e.toString())
    }

    /**
     * 사이트 정보 가져오기.
     */
    fun getSite() = tabRef?.addChildEventListener(firebaseTabRefListener)


    fun setFavoriteMode(isFavorite: Boolean) {
        _isFavoriteMode.value = isFavorite
    }

    fun setSearchText(text: String) {
        _searchText.value = text
    }

    override fun intentUrl(url: String) {
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
        _searchSiteZip.value = setFavoriteSite(site, true)
        compositeDisposable += siteRepository.insert(site = site)
    }

    override fun releaseFavorite(site: Site) {
        _searchSiteZip.value = setFavoriteSite(site, false)
        compositeDisposable += siteRepository.delete(
            primaryKey = site.sitePrimaryKey
        )
    }

    private fun setFavoriteSite(site: Site, isFavorite: Boolean): SiteZip {
        val tempSiteType = siteZip.copy()
        tempSiteType.siteList[tempSiteType.siteList.indexOfFirst {
            it.sitePrimaryKey == site.sitePrimaryKey
        }] = site.copy(isFavorite = isFavorite)
        return tempSiteType
    }

    /**
     * 로컬 데이베이스내 즐겨찾기 사이트인지 조회.
     */
    private fun checkLocalFavoriteSite(
        primaryKey: String,
        onComplete: ((isFavorite: Boolean) -> Unit),
        onError: (e: Throwable) -> Unit
    ) {
        compositeDisposable += siteRepository.checkFavoriteSite(primaryKey)
            .firstOrError()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribeBy(
                onSuccess = { isFavorite ->
                    onComplete(isFavorite)
                },
                onError = {
                    onError(it)
                }
            )
    }

    /**
     * 진행 다이어로그 표시 체크 설정.
     */
    fun setCheckVisibleProgress(isVisible: Boolean) {
        _checkVisibleProgress.postValue = isVisible
    }

    /**
     * 파이어베이스 typeRef 리스너 제거.
     */
    private fun removeFirebaseListener() {
        tabRef?.removeEventListener(firebaseTabRefListener)
    }

    /**
     * 바인딩.
     */
    fun onBind(item: SiteZip) {
        siteZip.siteList.clear()
        siteZip = item

        removeFirebaseListener()

        tabRef = null
        tabRef = makeTabRef(item)
        tabRef?.addChildEventListener(firebaseTabRefListener)
    }

    override fun onCleared() {
        super.onCleared()
        removeFirebaseListener()
    }
}