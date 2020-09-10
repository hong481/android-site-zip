package kr.co.hongstudio.sitezip.data.local.preference

import android.content.Context
import android.content.SharedPreferences
import kr.co.hongstudio.sitezip.util.extension.clear
import kr.co.hongstudio.sitezip.util.extension.putInt

class AdMobPreferenceImpl(

    applicationContext: Context

) : AdMobPreference {

    companion object {
        const val PREF_NAME = "admob_pref"
    }

    object Key {
        const val SHOW_INTERSTITIAL_ADMOB_COUNT = "SHOW_INTERSTITIAL_ADMOB_COUNT"
    }

    private val pref: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 전면광고 카운팅 숫자. (default = 0)
     */
    override var showInterstitialAdCount: Int
        get() = pref.getInt(Key.SHOW_INTERSTITIAL_ADMOB_COUNT, 0)
        set(value) {
            pref.putInt(Key.SHOW_INTERSTITIAL_ADMOB_COUNT, value)
        }

    /**
     * 초기화.
     */
    override fun clear() = pref.clear()

}

interface AdMobPreference {
    var showInterstitialAdCount: Int

    fun clear()
}