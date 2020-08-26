package kr.co.honga.sitezip.admob

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd

class AdMobManager(
    applicationContext: Context
) {

    private var interstitialAd: InterstitialAd = InterstitialAd(applicationContext).apply {
        adUnitId = "ca-app-pub-3940256099942544/1033173712"
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