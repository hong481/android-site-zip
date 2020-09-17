package kr.co.hongstudio.sitezip.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class UseCaseResponse constructor(

    @Json(name = Field.CODE)
    open var code: Int? = null,

    @Json(name = Field.MESSAGE)
    open var message: String? = null

) {

    object Field {
        const val CODE: String = "code"
        const val MESSAGE: String = "message"
    }

    object Code {
        const val SUCCESS: Int = 200
        const val FAIL: Int = 400
    }

    fun task(
        onSuccess: () -> Unit,
        onFail: () -> Unit
    ) {
        when (code) {
            Code.SUCCESS -> onSuccess()
            else -> onFail()
        }
    }

}