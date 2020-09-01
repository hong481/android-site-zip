package kr.co.hongstudio.sitezip.util.extension

import java.text.SimpleDateFormat
import java.util.*

private val dateFormats: MutableMap<String, SimpleDateFormat> by lazy {
    mutableMapOf<String, SimpleDateFormat>()
}

private fun findOrCrateFormatter(format: String): SimpleDateFormat {
    var formatter: SimpleDateFormat? = dateFormats[format]
    if (formatter == null) {
        formatter = SimpleDateFormat(format, Locale.getDefault())
        dateFormats += format to formatter
    }
    return formatter
}

infix fun Date?.format(format: String): String {
    if (this == null) {
        return ""
    }
    return findOrCrateFormatter(format).format(this)
}

infix fun String?.parseDate(format: String): Date? {
    if (this.isNullOrEmpty()) {
        return null
    }
    return findOrCrateFormatter(format).parse(this)

}
