package kr.co.hongstudio.sitezip.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BuildProperty(

    @Json(name = Field.BUILD_TYPE)
    val buildType: Int,

    @Json(name = Field.PRODUCT_NAME)
    val productName: String,

    @Json(name = Field.APP_TITLE_NAME)
    val appTitleName: String,

    @Json(name = Field.USE_GOOGLE_ADMOB)
    val useGoogleAdmob: Boolean

) {
    object Field {
        const val BUILD_TYPE: String = "build_type"
        const val PRODUCT_NAME: String = "product_name"
        const val APP_TITLE_NAME: String = "app_title_name"
        const val USE_GOOGLE_ADMOB: String = "use_google_admob"
    }
}