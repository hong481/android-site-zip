package kr.co.honga.sitezip.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BuildProperty(

    @Json(name = Field.BUILD_TYPE)
    val buildType: Int

) {
    object Field {
        const val BUILD_TYPE: String = "build_type"
    }
}