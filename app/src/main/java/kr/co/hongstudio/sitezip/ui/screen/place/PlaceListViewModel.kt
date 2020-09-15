package kr.co.hongstudio.sitezip.ui.screen.place

import android.graphics.Point
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kr.co.hongstudio.sitezip.base.viewmodel.BaseViewModel

class PlaceListViewModel : BaseViewModel() {

    companion object {
        const val TAG: String = "SiteZipViewModel"
    }

    object Serializable {
        const val PLACE: String = "PlaceListViewModel.Serializable.PLACE"
    }

    /**
     * 현재 위치 정보.
     */
    private val _location: MutableLiveData<Point> = MutableLiveData()
    val location: LiveData<Point> = _location
}