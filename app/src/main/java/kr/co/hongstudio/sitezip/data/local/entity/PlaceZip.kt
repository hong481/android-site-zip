package kr.co.hongstudio.sitezip.data.local.entity

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import kr.co.hongstudio.sitezip.base.model.Model

@IgnoreExtraProperties
@Parcelize
@JsonClass(generateAdapter = true)
data class PlaceZip(

    /**
     * 아이디.
     */
    override var id: Long? = 0L,

    /**
     * 순서.
     */
    override var index: Int? = 0,

    /**
     * 상태.
     */
    override var state: Int? = 0,

    /**
     * 탭 이름.
     */
    var tabName: String = "",

    /**
     * 탭 아이콘 url.
     */
    var tabIconUrl: String = "",


    /**
     * 기본 쿼리.
     */
    var defaultQuery: String = "",

    /**
     * 장소 검색 api.
     */
    var placeApi: String = "",

    /**
     * api 키.
     */
    var apiKey: String = "",

    /**
     * 장소 리스트.
     */
    var places: MutableList<Place> = mutableListOf()

) : Parcelable, Model {

    companion object {
        const val DEFAULT_QUERY = "defaultQuery"
        const val PLACE: String = "place"
    }

}