package kr.co.hongstudio.sitezip.data.local.entity

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize
import kr.co.hongstudio.sitezip.base.model.Model

@IgnoreExtraProperties
@Parcelize
data class Place(

    /**
     * 아이디.
     */
    override var id: Long? = 0L,

    /**
     * 순서.
     */
    override var index: Int? = 0,

    /**
     * 유형.
     */
    var typeName: String = "",

    /**
     * 탭 아이콘 url.
     */
    var tabIconUrl: String = ""

) : Parcelable, Model