package kr.co.hongstudio.sitezip.data.local.entity

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import com.squareup.moshi.Json
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
    @Json(name = "id")
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
     * 장소명.
     */
    @Json(name = "name")
    var name: String = "",

    /**
     * 카테고리.
     */
    @Json(name = "category")
    var category: String = "",

    /**
     * 이미지 소스 url
     */
    @Json(name = "imageSrc")
    var imageSrc: String = "",

    /**
     * 인터넷 전화번호.
     */
    @Json(name = "virtualPhone")
    var virtualPhone: String = "",

    /**
     * 일반 전화번호.
     */
    @Json(name = "phone")
    var phone: String = "",

    /**
     * 맵 url.
     */
    @Json(name = "routeUrl")
    var routeUrl: String = "",

    /**
     * 로드뷰 url.
     */
    @Json(name = "streetViewUrl")
    var streetViewUrl: String = "",

    /**
     * 도로명.
     */
    @Json(name = "roadAddr")
    var roadAddr: String = "",

    /**
     * 현재 주소.
     */
    @Json(name = "commonAddr")
    var commonAddr: String = "",

    /**
     * 일반 주소.
     */
    @Json(name = "addr")
    var addr: String = "",

    /**
     * 고객 리뷰 수.
     */
    @Json(name = "visitorReviewCount")
    var visitorReviewCount: String = "",

    /**
     * 고객 리뷰 점수.
     */
    @Json(name = "visitorReviewScore")
    var visitorReviewScore: String = "",

    /**
     * 영업 시간
     */
    @Json(name = "bizHourInfo")
    var bizHourInfo: String = "",

    /**
     * 위도 좌표값.
     */
    @Json(name = "x")
    var x: Long = 0L,

    /**
     * 경도 좌표값.
     */
    @Json(name = "y")
    var y: Long = 0L


) : Parcelable, Model