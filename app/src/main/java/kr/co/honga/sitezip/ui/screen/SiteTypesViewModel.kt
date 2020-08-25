package kr.co.honga.sitezip.ui.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import io.reactivex.rxjava3.kotlin.plusAssign
import kr.co.honga.sitezip.R
import kr.co.honga.sitezip.base.livedata.Event
import kr.co.honga.sitezip.base.viewmodel.BaseViewModel
import kr.co.honga.sitezip.data.local.entity.Site
import kr.co.honga.sitezip.data.local.entity.SiteType
import kr.co.honga.sitezip.repositories.repository.SiteRepository
import kr.co.honga.sitezip.util.ClipboardUtil
import kr.co.honga.sitezip.util.LogUtil
import kr.co.honga.sitezip.util.ResourceProvider
import kr.co.honga.sitezip.util.extension.notify
import kr.co.honga.sitezip.util.extension.refresh
import kr.co.honga.sitezip.util.extension.setValueIfNew

class SiteTypesViewModel(

    savedStateHandle: SavedStateHandle,
    private val siteRepository: SiteRepository,
    private val clipboardUtil: ClipboardUtil,
    private val resourceProvider: ResourceProvider

) : BaseViewModel(), SiteViewHolder.ViewModel {


    object Serializable {
        const val SITE_TYPE: String = "SitesViewModel.Serializable.SITE_TYPE"
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
    private val _siteType: MutableLiveData<SiteType> =
        savedStateHandle.getLiveData(Serializable.SITE_TYPE)
    val siteType: LiveData<SiteType> = _siteType

    /**
     * 사이트 유형.
     */
    private val _searchSiteType: MutableLiveData<SiteType> = MutableLiveData()
    val searchSiteType: LiveData<SiteType> = _searchSiteType

    /**
     * 즐겨찾기만 표시 여부.
     */
    private val _isFavoriteMode: MutableLiveData<Boolean> = MutableLiveData()

    fun getDisplaySiteType() {
        if (_isFavoriteMode.value == true) {
            val tempSiteType = _siteType.value?.copy() ?: return
            tempSiteType.apply {
                siteList = tempSiteType.siteList.filter {
                    it.isFavorite
                }.toMutableList()
            }
            _searchSiteType.value = tempSiteType
        } else {
            _searchSiteType.value = siteType.value
        }
    }

    fun setFavoriteMode(isFavorite: Boolean) {
        _isFavoriteMode.value = isFavorite
    }

    override fun intentUrl(url: String) {
        _intentUrlEvent.notify = url
    }

    override fun copyLink(label: String, text: String) {
        LogUtil.d(MainViewModel.TAG, "clipBoard. label : $label / text : $text")
        clipboardUtil.copyText(label, text)
        showToast(resourceProvider.getString(R.string.copy_to_link_message))
    }

    override fun chooseFavorite(site: Site) {
        _siteType.value = setFavoriteSite(site, true)
        _siteType.refresh(true)
        compositeDisposable += siteRepository.insert(site = site)
    }

    override fun releaseFavorite(site: Site) {
        _siteType.value = setFavoriteSite(site, false)
        _siteType.refresh(true)
        compositeDisposable += siteRepository.delete(
            primaryKey = site.sitePrimaryKey
        )
    }

    private fun setFavoriteSite(site: Site, isFavorite: Boolean): SiteType? {
        val tempSiteType = _siteType.value ?: return null
        tempSiteType.siteList[tempSiteType.siteList.indexOfFirst {
            it.sitePrimaryKey == site.sitePrimaryKey
        }] = site.copy(isFavorite = isFavorite)
        return tempSiteType
    }

    fun onBind(item: SiteType) {
        _siteType setValueIfNew item
    }
}