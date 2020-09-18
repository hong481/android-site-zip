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
    var name: String? = null,

    /**
     * 카테고리.
     */
    var category: String? = null,

    /**
     * 이미지 소스 url.
     */
    var imageSrc: String? = null,

    /**
     * 인터넷 전화번호.
     */
    var virtualPhone: String? = null,

    /**
     * 일반 전화번호.
     */
    var phone: String? = null,

    /**
     * 맵 url.
     */
    var routeUrl: String? = null,

    /**
     * 로드뷰 url.
     */
    var streetViewUrl: String? = null,

    /**
     * 도로명.
     */
    var roadAddr: String? = null,

    /**
     * 현재 주소.
     */
    var commonAddr: String? = null,

    /**
     * 일반 주소.
     */
    var addr: String? = null,

    /**
     * 고객 리뷰 수.
     */
    var visitorReviewCount: String? = null,

    /**
     * 고객 리뷰 점수.
     */
    var visitorReviewScore: String? = null,

    /**
     * 영업 시간.
     */
    var bizHourInfo: String? = null,

    /**
     * 위도 좌표값.
     */
    var x: String? = null,

    /**
     * 경도 좌표값.
     */
    var y: String? = null


) : Parcelable, Model