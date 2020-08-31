package kr.co.honga.sitezip.billing

import android.app.Activity
import com.android.billingclient.api.*
import kr.co.honga.sitezip.data.local.preference.BillingPreference

class BillingManager(
    private val activity: Activity,
    private val billingPref: BillingPreference
) : PurchasesUpdatedListener {

    companion object {
        const val REMOVE_ADS: String = "remove_ads"
    }

    val billingClient: BillingClient by lazy {
        BillingClient.newBuilder(activity).enablePendingPurchases().setListener(this).build()
    }

    fun makeBillingClient(): BillingClient =
        BillingClient.newBuilder(activity).enablePendingPurchases().setListener(this).build()

    fun getPurchaseHistory() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {}

            override fun onBillingSetupFinished(billingResult: BillingResult?) {
                billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP) { result, purchaseHistoryRecordList ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        billingPref.removeAds = purchaseHistoryRecordList != null && purchaseHistoryRecordList.size > 0
                    }
                }
            }
        })
    }

    fun processToPurchase() {
        val skuList = ArrayList<String>()
        skuList.add(REMOVE_ADS)
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
            }

            override fun onBillingSetupFinished(billingResult: BillingResult?) {
                val params = SkuDetailsParams.newBuilder()
                params.setSkusList(skuList)
                params.setType(BillingClient.SkuType.INAPP)
                launchBillingFlow(params)
            }
        })
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult?,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                billingPref.removeAds = false
                handlePurchase(purchase)
            }
        }
    }

    private fun launchBillingFlow(params: SkuDetailsParams.Builder) {
        billingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (skuDetailsList.size > 0) {
                    val flowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetailsList[0])
                        .build()
                    billingClient.launchBillingFlow(activity, flowParams)
                }
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams
            .newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .setDeveloperPayload(purchase.developerPayload)
            .build()

        billingClient.consumeAsync(consumeParams) { _, _ -> }
    }
}
