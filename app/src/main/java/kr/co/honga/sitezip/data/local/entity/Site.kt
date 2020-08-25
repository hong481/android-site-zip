package kr.co.honga.sitezip.data.local.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
@Entity
data class Site(

    /**
     * 아이디.
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name ="id")
    var id: Long = 0L,

    /**
     * 사이트 고유 키값.
     */
    @ColumnInfo(name = "site_primary_key")
    var sitePrimaryKey: String = "",

    /**
     * 사이트 명.
     */
    @ColumnInfo(name = "site_name")
    var siteName: String = "",

    /**
     * 즐겨 찾기 여부.
     */
    @ColumnInfo(name = "is_favorite")
    var isFavorite: Boolean = false,

    /**
     * 사이트 짧은 설명.
     */
    @ColumnInfo(name = "sht_info")
    var shortInfo: String = "",

    /**
     * 사이트 주소.
     */
    @ColumnInfo(name = "site_link")
    var siteLink: String = "",

    /**
     * 사이트 아이콘 url.
     */
    @ColumnInfo(name = "site_icon_url")
    var siteIconUrl: String = ""

) : Parcelable