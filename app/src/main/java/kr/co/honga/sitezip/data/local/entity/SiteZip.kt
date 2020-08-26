package kr.co.honga.sitezip.data.local.entity

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
data class SiteZip (

    var typeName: String = "",

    var siteList: MutableList<Site> = mutableListOf()

) : Parcelable