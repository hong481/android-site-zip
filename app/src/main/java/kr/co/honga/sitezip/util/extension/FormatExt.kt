package kr.co.honga.sitezip.util.extension

import java.net.URLDecoder
import java.net.URLEncoder

/**
 * URL 인코딩 (UTF-8)
 */
fun String?.toUrlEncode(): String = when (this.isNullOrEmpty()) {
    true -> ""
    false -> URLEncoder.encode(this, "UTF-8")
}

/**
 * URL 디코딩 (UTF-8)
 */
fun String?.toUrlDecode(): String = when (this.isNullOrEmpty()) {
    true -> ""
    false -> URLDecoder.decode(this, "UTF-8")
}


/**
 * 특수문자 제거.
 */
fun String.removeSpecialCharacters(): String =
    replace("[\\\\|*\"?:/<>]".toRegex(), "")