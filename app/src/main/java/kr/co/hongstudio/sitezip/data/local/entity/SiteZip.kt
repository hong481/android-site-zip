package kr.co.hongstudio.sitezip.data.local.entity

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize
import kr.co.hongstudio.sitezip.base.model.Model

@IgnoreExtraProperties
@Parcelize
data class SiteZip(

    /**
     * 아이디.
     */
    override var id: Long? = 0L,

    /**
     * 순서.
     */
    override var index: Int? = 0,

    /**
     * 탭 이름.
     */
    var tabName: String = "",

    /**
     * 탭 아이콘 url.
     */
    var tabIconUrl: String = "",

    /**
     * 사이트 리스트.
     */
    var siteList: MutableList<Site> = mutableListOf()

) : Parcelable, Model {
    companion object {
        const val SITE: String = "site"
    }
}