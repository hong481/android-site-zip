package kr.co.hongstudio.sitezip.googleplay

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory

class InAppReviewManager(
    applicationContext: Context
) {

    companion object {
        const val TAG: String = "InAppReviewManager"
    }

    private val reviewManager: ReviewManager = ReviewManagerFactory.create(applicationContext)

    /**
     * 인앱 리뷰 창 보여주기.
     */
    fun showInAppReview(
        activity: Activity,
        onComplete: () -> Unit,
        onFail: () -> Unit
    ) {
        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { requestComplete ->
            if (requestComplete.isSuccessful) {
                val reviewInfo: ReviewInfo = requestComplete.result
                val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener {
                    Log.d(TAG, "showInAppReview. flow complete.")
                    onComplete()
                }
            } else {
                Log.d(TAG, "showInAppReview. fail.")
                onFail()
            }

        }
    }
}