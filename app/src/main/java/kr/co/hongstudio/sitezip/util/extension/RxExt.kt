package kr.co.hongstudio.sitezip.util.extension

import android.util.Log
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import io.reactivex.Completable as CompletableV2
import io.reactivex.Flowable as FlowableV2
import io.reactivex.Maybe as MaybeV2
import io.reactivex.Observable as ObservableV2
import io.reactivex.Single as SingleV2

private const val TAG: String = "RxExt"

object MySchedulers {
    /**
     * 현재 스케줄러를 리턴.
     */
    fun current(): Scheduler = Schedulers.from { it.run() }
}

/**
 * 타이머.
 */
fun timer(delay: Long, onDelayed: () -> Unit): Disposable = Completable.timer(delay, TimeUnit.MILLISECONDS)
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(onDelayed, {
        Log.d(TAG, it.toString())
    })

/**
 * 메인 스레드 샐행.
 */
fun runOnMainThread(runnable: () -> Unit): Disposable = Observable.empty<Unit>()
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribeBy(
        onComplete = runnable,
        onError = {
            Log.d(TAG, it.toString())
        }
    )

/**
 * 백그라운드 스레드 실행.
 */
fun runOnBackgroundThread(runnable: () -> Unit): Disposable = Observable.empty<Unit>()
    .subscribeOn(Schedulers.io())
    .observeOn(Schedulers.io())
    .subscribeBy(
        onComplete = runnable,
        onError = {
//            Log.d(TAG, it.toString())
        }
    )

/**
 * 빈 disposable 리턴.
 */
fun emptyDisposable(): Disposable = Disposable.empty()

/**
 * Observable v2 to v3.
 */
fun <T> ObservableV2<T>.toV3(): Observable<T> = RxJavaBridge.toV3Observable(this)

/**
 * Flowable v2 to v3.
 */
fun <T> FlowableV2<T>.toV3(): Flowable<T> = RxJavaBridge.toV3Flowable(this)

/**
 * Single v2 to v3.
 */
fun <T> SingleV2<T>.toV3(): Single<T> = RxJavaBridge.toV3Single(this)

/**
 * Maybe v2 to v3.
 */
fun <T> MaybeV2<T>.toV3(): Maybe<T> = RxJavaBridge.toV3Maybe(this)

/**
 * Completable v2 to v3.
 */
fun <T> CompletableV2.toV3(): Completable = RxJavaBridge.toV3Completable(this)