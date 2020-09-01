package kr.co.hongstudio.sitezip.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kr.co.hongstudio.sitezip.data.local.preference.BillingPreference

class BillingManager(

    private val applicationContext: Context,
    private val billingPref: BillingPreference

) : PurchasesUpdatedListener {

    companion object {

        const val TAG: String = "BillingManager"

        const val REMOVE_ADS: String = "remove_ads"
    }

    private lateinit var billingClient: BillingClient


    /**
     * 구글 플레이 연결.
     */
    fun connectGooglePlay() {
        billingClient =
            BillingClient.newBuilder(applicationContext).setListener(this).enablePendingPurchases()
                .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP) { _, purchaseHistoryRecordList ->
                        billingPref.removeAds =
                            purchaseHistoryRecordList != null && purchaseHistoryRecordList.size > 0
                    }
                    Log.d(
                        TAG,
                        "connect google pay server success. removeAds : ${billingPref.removeAds}"
                    )
                } else {
                    Log.d(
                        TAG,
                        "connect google pay server fail. error code : ${billingResult.responseCode}"
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "disconnect google pay server.")
            }
        })
    }

    /**
     * 구매 진행.
     */
    fun processToPurchase() {
        Log.d(TAG, "processToPurchase.")
        val skuList = ArrayList<String>().apply {
            add(REMOVE_ADS)
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val params = SkuDetailsParams.newBuilder()
                params.setSkusList(skuList)
                params.setType(BillingClient.SkuType.INAPP)
                launchBillingFlow(params)
            }

            override fun onBillingServiceDisconnected() {}
        })
    }

    private fun launchBillingFlow(params: SkuDetailsParams.Builder) {
        billingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "launchBillingFlow. BillingClient.BillingResponseCode.OK.")
                if (skuDetailsList != null) {
                    if (skuDetailsList.size > 0) {
                        val flowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuDetailsList[0])
                            .build()
                        val responseCode = billingClient.launchBillingFlow(
                            applicationContext as Activity,
                            flowParams
                        )
                        Log.d(TAG, "launchBillingFlow. responseCode : $responseCode")
                    }
                }
            }
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                billingPref.removeAds = true
                handlePurchase(purchase)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams
            .newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.consumeAsync(consumeParams) { _, _ -> }
    }

}
