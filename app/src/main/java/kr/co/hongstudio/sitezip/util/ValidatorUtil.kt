package kr.co.hongstudio.sitezip.util

import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL
import java.util.regex.Matcher
import java.util.regex.Pattern

class ValidatorUtil {

    /**
     * URL 형식인지 체크.
     */
    fun checkUrl(text: String?): Boolean {
        val pattern: Pattern = Pattern.compile("^(?:https?:\\/\\/)?(?:www\\.)?[a-zA-Z0-9./]+$")
        val matcher: Matcher = pattern.matcher(text)
        if (matcher.matches()) return true
        val url: URL?
        try {
            url = URL(text)
        } catch (e: MalformedURLException) {
            return false
        }
        try {
            url.toURI()
        } catch (e: URISyntaxException) {
            return false
        }
        return true
    }
}