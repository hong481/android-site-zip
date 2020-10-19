package kr.co.hongstudio.sitezip.ui.screen.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kr.co.hongstudio.sitezip.BuildConfig
import kr.co.hongstudio.sitezip.base.viewmodel.BaseViewModel
import kr.co.hongstudio.sitezip.data.local.preference.AppPreference

class SettingViewModel(

    appPref: AppPreference

) : BaseViewModel() {

    companion object {
        const val TAG: String = "SettingViewModel"
    }

    /**
     * 앱 버전.
     */
    val appVersion: LiveData<String> = MutableLiveData<String>(BuildConfig.VERSION_NAME)

    /**
     * 앱 리뷰 요청 팝업 활성화 여부.
     */
    val visibleAppiraterDialog: MutableLiveData<Boolean> = MutableLiveData(appPref.visibleAppiraterDialog)

    /**
     * 앱 리뷰 요청 팝업 활성화 설정.
     */
    fun setVisibleAppiraterDialog(isVisible: Boolean) {
        visibleAppiraterDialog.value = isVisible
    }
}