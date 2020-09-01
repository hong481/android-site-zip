package kr.co.hongstudio.sitezip.data.local.preference

import android.content.Context
import android.content.SharedPreferences
import kr.co.hongstudio.sitezip.billing.BillingManager.Companion.REMOVE_ADS
import kr.co.hongstudio.sitezip.util.extension.clear
import kr.co.hongstudio.sitezip.util.extension.putBoolean

class BillingPreferenceImpl(

    applicationContext: Context

) : BillingPreference {

    companion object {
        const val PREF_NAME = "billing_pref"
    }

    private val pref: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 광고삭제 여부. (default = false)
     */
    override var removeAds: Boolean
        get() = pref.getBoolean(REMOVE_ADS, false)
        set(value) {
            pref.putBoolean(REMOVE_ADS, value)
        }

    /**
     * 초기화.
     */
    override fun clear() = pref.clear()

}

interface BillingPreference {
    var removeAds: Boolean

    fun clear()
}