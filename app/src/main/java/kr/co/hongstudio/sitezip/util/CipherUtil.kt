package kr.co.hongstudio.sitezip.util

import android.util.Base64
import java.net.URLDecoder
import java.net.URLEncoder

class CipherUtil{
    companion object {
        const val TAG: String = "CipherUtil"
    }
}

/**
 * Base64 인코딩.
 */
fun String.encodeBase64(flags: Int = Base64.NO_WRAP): String = try {
    Base64.encodeToString(toByteArray(), flags)
} catch (e: Exception) {
    LogUtil.exception(CipherUtil.TAG, e)
    ""
}

/**
 * Base64 디코딩.
 */
fun String.decodeBase64(flags: Int = Base64.NO_WRAP): String = try {
    String(Base64.decode(toByteArray(), flags))
} catch (e: Exception) {
    LogUtil.exception(CipherUtil.TAG, e)
    ""
}
