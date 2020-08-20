package kr.co.honga.sitezip.ui.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import kr.co.honga.sitezip.R
import kr.co.honga.sitezip.base.livedata.Event
import kr.co.honga.sitezip.base.viewmodel.BaseViewModel
import kr.co.honga.sitezip.data.local.entity.SiteType
import kr.co.honga.sitezip.util.ClipboardUtil
import kr.co.honga.sitezip.util.LogUtil
import kr.co.honga.sitezip.util.ResourceProvider
import kr.co.honga.sitezip.util.extension.notify

class SiteTypesViewModel(

    savedStateHandle: SavedStateHandle,
    private val clipboardUtil: ClipboardUtil,
    private val resourceProvider: ResourceProvider

) : BaseViewModel(), SiteViewHolder.ViewModel {


    object Serializable {
        const val SITE: String = "SitesViewModel.Serializable.SITE"
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
     * 녹취 내역
     */
    val siteType: MutableLiveData<SiteType> = savedStateHandle.getLiveData(Serializable.SITE)

    override fun intentUrl(url: String) {
        _intentUrlEvent.notify = url
    }

    override fun copyLink(label: String, text: String) {
        LogUtil.d(MainViewModel.TAG, "clipBoard. label : $label / text : $text")
        clipboardUtil.copyText(label, text)
        showToast(resourceProvider.getString(R.string.copy_to_link_message))
    }
}