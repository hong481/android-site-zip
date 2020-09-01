package kr.co.hongstudio.sitezip.ui.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import io.reactivex.rxjava3.kotlin.plusAssign
import kr.co.hongstudio.sitezip.R
import kr.co.hongstudio.sitezip.admob.AdMobManager
import kr.co.hongstudio.sitezip.base.livedata.Event
import kr.co.hongstudio.sitezip.base.viewmodel.BaseViewModel
import kr.co.hongstudio.sitezip.data.local.entity.Site
import kr.co.hongstudio.sitezip.data.local.entity.SiteZip
import kr.co.hongstudio.sitezip.repositories.repository.SiteRepository
import kr.co.hongstudio.sitezip.util.ClipboardUtil
import kr.co.hongstudio.sitezip.util.LogUtil
import kr.co.hongstudio.sitezip.util.ResourceProvider
import kr.co.hongstudio.sitezip.util.extension.notify
import kr.co.hongstudio.sitezip.util.extension.refresh
import kr.co.hongstudio.sitezip.util.extension.setValueIfNew
import java.util.*

class SiteZipViewModel(

    savedStateHandle: SavedStateHandle,
    private val siteRepository: SiteRepository,
    private val clipboardUtil: ClipboardUtil,
    private val resourceProvider: ResourceProvider,
    private val adMobManager: AdMobManager

) : BaseViewModel(), SiteViewHolder.ViewModel {


    object Serializable {
        const val SITE_ZIP: String = "SitesViewModel.Serializable.SITE_ZIP"
    }

    companion object {
        const val TAG: String = "SitesViewModel"
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
     * 즐겨찾기만 표시 여부.
     */
    private val _isFavoriteMode: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * 검색 텍스트.
     */
    private val _searchText: MutableLiveData<String> = MutableLiveData()

    fun getDisplaySiteType() {
        val tempSiteZip: SiteZip = _siteZip.value?.copy() ?: return
        tempSiteZip.apply {
            siteList = if (_isFavoriteMode.value == true) {
                tempSiteZip.siteList.filter {
                    it.isFavorite && (it.title.contains(_searchText.value ?: "", ignoreCase = true)
                            || it.url.contains(_searchText.value ?: "", ignoreCase = true))
                }.toMutableList()
            } else {
                tempSiteZip.siteList.filter {
                    it.title.contains(_searchText.value ?: "", ignoreCase = true)
                            || it.url.contains(_searchText.value ?: "", ignoreCase = true)
                }.toMutableList()
            }
            siteList.sortBy {
                it.id
            }
        }
        _searchSiteZip.value = tempSiteZip
    }

    fun setFavoriteMode(isFavorite: Boolean) {
        _isFavoriteMode.value = isFavorite
    }

    fun setSearchText(text: String) {
        _searchText.value = text
    }

    override fun intentUrl(url: String) {

        val num = Random().nextInt(3)
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

    fun onBind(item: SiteZip) {
        _siteZip setValueIfNew item
    }
}