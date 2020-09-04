package kr.co.hongstudio.sitezip.data.local.entity

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
data class SiteZip (

    var typeName: String = "",
    var index: Int = 0,
    var siteList: MutableList<Site> = mutableListOf()

) : Parcelable {
    companion object {
        const val INDEX : String =  "index"
    }
}