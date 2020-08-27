package kr.co.honga.sitezip.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BuildProperty(

    @Json(name = Field.BUILD_TYPE)
    val buildType: Int,

    @Json(name = Field.PRODUCTS)
    val products: String,

    @Json(name = Field.USE_GOOGLE_ADMOB)
    val useGoogleAdmob: Boolean

) {
    object Field {
        const val BUILD_TYPE: String = "build_type"
        const val PRODUCTS: String = "products"
        const val USE_GOOGLE_ADMOB: String = "use_google_admob"
    }
}