package kr.co.hongstudio.sitezip.data.local.preference

import android.content.Context
import android.content.SharedPreferences
import kr.co.hongstudio.sitezip.util.extension.clear
import kr.co.hongstudio.sitezip.util.extension.putBoolean

class AppPreferenceImpl(

    applicationContext: Context

) : AppPreference {

    companion object {
        const val PREF_NAME = "app_pref"
    }

    object Key {
        const val VISIBLE_APPIRATER_DIALOG = "VISIBLE_APPIRATER_DIALOG"
    }

    private val pref: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 앱 리뷰 다이어로그 표시 안함 여부. (default = true)
     */
    override var visibleAppiraterDialog: Boolean
        get() = pref.getBoolean(Key.VISIBLE_APPIRATER_DIALOG, true)
        set(value) {
            pref.putBoolean(Key.VISIBLE_APPIRATER_DIALOG, value)
        }

    override fun registerChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        pref.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unregisterChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        pref.unregisterOnSharedPreferenceChangeListener(listener)
    }

    /**
     * 초기화.
     */
    override fun clear() = pref.clear()

}

interface AppPreference {
    var visibleAppiraterDialog: Boolean

    fun registerChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener)

    fun unregisterChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener)

    fun clear()
}