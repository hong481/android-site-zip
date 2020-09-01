package kr.co.hongstudio.sitezip.admob

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import kr.co.hongstudio.sitezip.R
import kr.co.hongstudio.sitezip.util.ResourceProvider

class AdMobManager(
    resourceProvider: ResourceProvider,
    applicationContext: Context
) {

    private var interstitialAd: InterstitialAd = InterstitialAd(applicationContext).apply {
        adUnitId = resourceProvider.getString(R.string.google_admob_interstitial_id_test)
        adListener = object : AdListener() {
            override fun onAdClosed() {
                loadAd(AdRequest.Builder().build())
            }
        }
        loadAd(AdRequest.Builder().build())
    }

    /**
     * 전면 광고 표시.
     */
    fun showInterstitialAd() {
        if (interstitialAd.isLoaded) {
            interstitialAd.show()
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.")
        }
    }

}