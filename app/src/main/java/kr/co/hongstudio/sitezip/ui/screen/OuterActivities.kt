package kr.co.hongstudio.sitezip.ui.screen

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import com.gun0912.tedonactivityresult.model.ActivityResult
import com.tedpark.tedonactivityresult.rx2.TedRxOnActivityResult
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.hongstudio.sitezip.util.extension.toV3
import java.util.*

object OuterActivities {

    const val TAG: String = "OuterActivities"

    /**
     * 음성 검색창 열기.
     */
    fun intentVoiceSearch(
        context: Context,
        onActivityResult: ((ActivityResult) -> Unit)? = null
    ): Disposable {
        val intent : Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREA);
        }
        return TedRxOnActivityResult.with(context)
            .startActivityForResult(intent)
            .toV3()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { activityResult ->
                    onActivityResult?.let {
                        it(activityResult)
                    }
                },
                onError = {
                    Log.d(TAG, it.toString())
                }
            )
    }
}