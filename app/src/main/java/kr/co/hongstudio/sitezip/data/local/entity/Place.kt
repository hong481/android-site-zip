package kr.co.hongstudio.sitezip.data.local.entity

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import kr.co.hongstudio.sitezip.base.model.Model

@IgnoreExtraProperties
@Parcelize
@JsonClass(generateAdapter = true)
data class Place(

    /**
     * 아이디.
     */
    override var id: Long? = null,

    /**
     * 순서.
     */
    override var index: Int? = null,

    /**
     * 상태.
     */
    override var state: Int? = null,

    /**
     * 장소명.
     */
    var place_name: String? = null,

    /**
     * 카테고리.
     */
    var category_name: String? = null,

    /**
     *  중요 카테고리만 그룹핑한 카테고리 그룹 코드.
     */
    var category_group_code: String? = null,

    /**
     * 중요 카테고리만 그룹핑한 카테고리 그룹명.
     */
    var category_group_name: String? = null,

    /**
     * 일반 전화번호.
     */
    var phone: String? = null,

    /**
     *  장소 상세화면
     */
    var place_url: String? = null,


    /**
     * 전체 도로명 주소.
     */
    var road_address_name: String? = null,

    /**
     * 전체 지번 주소.
     */
    var address_name: String? = null,

    /**
     * 중심좌표까지의 거리 (단, x,y 파라미터를 준 경우에만 존재).
     */
    var distance: String? = null,

    /**
     * 위도 좌표값.
     */
    var x: String? = null,

    /**
     * 경도 좌표값.
     */
    var y: String? = null


) : Parcelable, Model