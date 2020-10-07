package kr.co.hongstudio.sitezip.ui.appirater

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kr.co.hongstudio.sitezip.R
import kr.co.hongstudio.sitezip.base.livedata.EmptyEvent
import kr.co.hongstudio.sitezip.base.livedata.Event
import kr.co.hongstudio.sitezip.base.viewmodel.BaseViewModel
import kr.co.hongstudio.sitezip.data.local.preference.AppPreference
import kr.co.hongstudio.sitezip.util.ResourceProvider
import kr.co.hongstudio.sitezip.util.extension.notify

class AppiraterDialogViewModel(

    resourceProvider: ResourceProvider,
    private val appPref: AppPreference

) : BaseViewModel() {

    companion object {
        const val TAG: String = "AppiraterDialogViewModel"

        const val BASE_MARKET_URL: String = "https://play.google.com/store/apps/details?id="
    }

    /**
     * 다이어로그 타이틀.
     */
    val title: LiveData<String> = MutableLiveData<String>(
        String.format(
            resourceProvider.getString(R.string.appirater_title),
            resourceProvider.getString(R.string.app_name)
        )
    )

    /**
     * 다이어로그 표시 여부.
     */
    val isVisibleDialog: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * 마켓 페이지로 이동.
     */
    private var _intentMarketPageEvent: MutableLiveData<Event<String>> = MutableLiveData()
    var intentMarketPageEvent: LiveData<Event<String>> = _intentMarketPageEvent

    /**
     * 다이어로크 닫기 이벤트.
     */
    private var _closeDialogEvent: MutableLiveData<EmptyEvent> = MutableLiveData()
    var closeDialogEvent: LiveData<EmptyEvent> = _closeDialogEvent

    /**
     * 다이어로그 표시 여부 설정.
     */
    fun setVisibleDialog(isVisible: Boolean) {
        appPref.visibleAppiraterDialog = isVisible
    }

    /**
     * 마켓 페이지로 이동.
     */
    fun intentMarketPage() {
        _intentMarketPageEvent.notify = BASE_MARKET_URL
    }

    /**
     *  다이어로그 닫기.
     */
    fun closeDialog() {
        _closeDialogEvent.notify()
    }

}