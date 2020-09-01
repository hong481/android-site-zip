package kr.co.hongstudio.sitezip.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context


class ClipboardUtil(
    applicationContext: Context
) {

    companion object {
        const val TAG: String = "ClipboardUtil"
    }

    private val clipboardManager: ClipboardManager by lazy {
        applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    /**
     * 텍스트 복사하기.
     */
    fun copyText(label: String, text: String) {
        val clip = ClipData.newPlainText(label, text)
        clipboardManager.setPrimaryClip(clip)
    }

}