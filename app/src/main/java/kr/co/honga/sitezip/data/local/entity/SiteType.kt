package kr.co.honga.sitezip.data.local.entity

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize
import kr.co.honga.sitezip.base.model.Model


@IgnoreExtraProperties
@Parcelize
data class SiteType (

    var typeName: String = "",

    var siteList: MutableList<Site> = mutableListOf()

) : Parcelable