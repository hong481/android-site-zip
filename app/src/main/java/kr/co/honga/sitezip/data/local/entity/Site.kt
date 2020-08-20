package kr.co.honga.sitezip.data.local.entity

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize


@IgnoreExtraProperties
@Parcelize
data class Site (

    var siteName: String = "",

    var shortInfo: String = "",

    var siteLink : String = "",

    var siteIconUrl: String = ""

) : Parcelable