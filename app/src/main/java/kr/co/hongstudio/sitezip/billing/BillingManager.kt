package kr.co.hongstudio.sitezip.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kr.co.hongstudio.sitezip.data.local.preference.BillingPreference
import java.util.*

class BillingManager(

    private val billingPref: BillingPreference,
    private val applicationContext: Context

) : PurchasesUpdatedListener {

    companion object {
        const val TAG: String = "BillingManager"
        const val REMOVE_ADS: String = "remove_ads"
        const val SUPPORT: String = "support"
    }

    private lateinit var billingClient: BillingClient

    /**
     * 아이템 상세정보 리스트.
     */
    private var skuDetailsListItem: List<SkuDetails> = mutableListOf()

    /**
     * 아이템 소비 리스너아이템 소비 리스너.
     */
    private val consumeListener: ConsumeResponseListener =
        ConsumeResponseListener { billingResult, purchaseToken ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "상품을 성공적으로 소모하였습니다. 소모된 상품 => $purchaseToken")
            } else {
                Log.d(
                    TAG,
                    "상품 소모에 실패하였습니다. 오류코드 (" + billingResult.responseCode + "), " +
                            "대상 상품 코드: " + purchaseToken
                )
            }
        }

    /**
     * 구글 플레이 연결.
     */
    fun connectGooglePlay() {
        Log.d(TAG, "구글 결제 매니저를 초기화 하고 있습니다.")
        billingClient = BillingClient.newBuilder(applicationContext).setListener(this).enablePendingPurchases().build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "구글 결제 서버에 접속을 성공하였습니다.")
                    billingPref.removeAds = false
                    getSkuDetailList()
                    // 소모가 안된 상품 존재시 - 리스트에 아이템이 존재하게 된다
                    val purchases: MutableList<Purchase> = billingClient.queryPurchases(BillingClient.SkuType.INAPP).purchasesList ?: mutableListOf()
                    if (purchases.size <= 0) {
                        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP) { _, purchaseHistories ->
                            for (item: PurchaseHistoryRecord in purchaseHistories ?: mutableListOf()) {
                                if (item.sku == REMOVE_ADS) {
                                    billingPref.removeAds = true
                                }
                            }
                        }
                    }
                    for (purchase: Purchase in purchases) {
                        // 계속 보류중일때
                        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
                            // 카드사 승인중인 결제 또는 결제 보류중
                        } else {
                            handlePurchase(purchase)
                        }
                    }
                } else {
                    Log.d(TAG, "구글 결제 서버 접속에 실패하였습니다.오류코드: ${billingResult.responseCode}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "구글 결제 서버와 접속이 끊어졌습니다.")
            }
        })
    }


    private fun getSkuDetailList() {
        // 구글 상품 정보들의 ID를 만들어 줌
        val skuIdListInApp: MutableList<String> = ArrayList()

        skuIdListInApp.add(REMOVE_ADS)
        skuIdListInApp.add(SUPPORT)
        // SkuDetailsList 객체를 만듬
        val paramsItem = SkuDetailsParams.newBuilder()
        paramsItem.setSkusList(skuIdListInApp).setType(BillingClient.SkuType.INAPP)

        // 비동기 상태로 앱의 정보를 가지고 옴
        billingClient.querySkuDetailsAsync(
            paramsItem.build(),
            SkuDetailsResponseListener { billingResult, skuDetailsList -> // 상품 정보를 가지고 오지 못한 경우
                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.d(
                        TAG, "(인앱) 상품 정보를 가지고 오던 중 오류가 발생했습니다. 오류코드: ${billingResult.responseCode}"
                    )
                    return@SkuDetailsResponseListener
                }
                if (skuDetailsList == null) {
                    Log.d(TAG, "(인앱) 상품 정보가 존재하지 않습니다.")
                    return@SkuDetailsResponseListener
                }
                //응답 받은 데이터들의 숫자를 출력
                Log.d(TAG, "(인앱) 응답 받은 데이터 숫자: " + skuDetailsList.size)

                //받아온 상품 정보를 차례로 호출
                for (skuDetails :SkuDetails in skuDetailsList) {
                    //해당 인덱스의 상품 정보를 출력
                    Log.d(TAG, "${skuDetails.sku}: ${skuDetails.title}, price: ${skuDetails.price}")
                    Log.d(TAG, skuDetails.originalJson)
                }

                //받은 값을 멤버 변수로 저장
                skuDetailsListItem = skuDetailsList
            })
    }

    /**
     * 실제 구입 처리를 하는 메소드.
     */
    fun processToPurchase(itemName: String, activity: Activity) {
        var skuDetails: SkuDetails? = null
        for (indices: Int in skuDetailsListItem.indices) {
            val details: SkuDetails = skuDetailsListItem[indices]
            if (details.sku == itemName) {
                skuDetails = details
                break
            }
        }
        skuDetails?.let {
            billingClient.launchBillingFlow(
                activity, BillingFlowParams.newBuilder()
                    .setSkuDetails(it)
                    .build()
            )
        }
    }

    /**
     * 결제 요청 후 상품에대해 소비시켜주는 메소드.
     */
    fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // 인앱 소비
            // TODO 인앱 구매 결과전송 함수 호출
            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.consumeAsync(consumeParams, consumeListener)
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            // ex 해당 아이템에 대해 소모되지 않은 결제가 있을시
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        //결제에 성공한 경우
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            Log.d(TAG, "결제에 성공했으며, 아래에 구매한 상품들이 나열됨")
            for (purchase: Purchase in purchases) {
                Log.d(TAG, "purchases.sku: ${purchase.sku}")
                if (purchase.sku == REMOVE_ADS) {
                    billingPref.removeAds = true
                }
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "사용자에 의해 결제취소")
        } else {
            Log.d(TAG, "결제가 취소 되었습니다. 종료코드: " + billingResult.responseCode)
            if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                // ex 해당 아이템에 대해 소모되지 않은 결제가 있을시
                // todo
            }
        }
    }
}