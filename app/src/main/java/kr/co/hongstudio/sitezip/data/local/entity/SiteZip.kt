package kr.co.hongstudio.sitezip.data.local.entity

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
data class SiteZip (

    /**
     * 유형.
     */
    var typeName: String = "",

    /**
     * 목록 순서.
     */
    var index: Int = 0,


    /**
     * 탭 아이콘 url.
     */
    var tabIconUrl: String = "",

    /**
     * 사이트 리스트.
     */
    var siteList: MutableList<Site> = mutableListOf()

) : Parcelable {
    companion object {
        const val INDEX : String =  "index"
        const val SITE : String =  "site"
    }
}