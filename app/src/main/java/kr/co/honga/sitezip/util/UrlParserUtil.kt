package kr.co.honga.sitezip.util

import kr.co.honga.sitezip.data.Metadata
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import java.util.regex.Matcher
import java.util.regex.Pattern

class UrlParserUtil(
    private val validatorUtil: ValidatorUtil
) {

    fun getMetadataFromUrl(url: String): Metadata? {
        return try {
            Metadata().apply {
                val doc: Document = Jsoup.connect(url).get()
                title = doc.select("meta[property=og:title]").first().attr("content")
                description = doc.select("meta[property=og:description]")[0].attr("content")
                imageUrl = doc.select("meta[property=og:image]")[0].attr("content")
            }
        } catch (e: Exception) {
            null
        }
    }

    fun extractUrlFromText(text: String): String {
        val urlRegex =
            "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)"
        val pattern: Pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE)
        val urlMatcher: Matcher = pattern.matcher(text)
        val urls: ArrayList<String> = ArrayList()
        while (urlMatcher.find()) {
            urls.add(text.substring(urlMatcher.start(0), urlMatcher.end(0)))
        }
        var resultUrl = ""
        for (url in urls) {
            if (validatorUtil.checkUrl(url)) {
                resultUrl = url
                break
            }
        }
        return resultUrl
    }
}