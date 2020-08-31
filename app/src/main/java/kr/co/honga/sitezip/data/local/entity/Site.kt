package kr.co.honga.sitezip.data.local.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize
import kr.co.honga.sitezip.base.model.Model

@IgnoreExtraProperties
@Parcelize
@Entity
data class Site(

    /**
     * 아이디.
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name ="id")
    override var id: Long? = 0L,

    /**
     * 사이트 고유 키값.
     */
    @ColumnInfo(name = "site_primary_key")
    var sitePrimaryKey: String = "",

    /**
     * 사이트 명.
     */
    @ColumnInfo(name = "title")
    var title: String = "",

    /**
     * 즐겨 찾기 여부.
     */
    @ColumnInfo(name = "is_favorite")
    var isFavorite: Boolean = false,

    /**
     * 사이트 설명.
     */
    @ColumnInfo(name = "description")
    var description: String = "",

    /**
     * 사이트 주소.
     */
    @ColumnInfo(name = "url")
    var url: String = "",

    /**
     * 사이트 아이콘 url.
     */
    @ColumnInfo(name = "icon_url")
    var iconUrl: String = "",

    /**
     * http 아이콘 사용여부
     */
    @ColumnInfo(name = "is_use_http_icon")
    var isUseHttpIcon : Boolean = false

) : Parcelable, Model {
    companion object {
        const val IS_USE_HTTP_ICON_VAR_NAME : String =  "isUseHttpIcon"
    }
}