package kr.co.hongstudio.sitezip

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Process
import androidx.core.app.ActivityCompat
import com.google.android.gms.ads.MobileAds
import io.reactivex.rxjava3.exceptions.UndeliverableException
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import kr.co.hongstudio.sitezip.di.AppModule
import kr.co.hongstudio.sitezip.observer.NetworkObserver
import kr.co.hongstudio.sitezip.util.DatabaseDebugUtil
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.fragment.koin.fragmentFactory
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import java.io.IOException
import java.net.SocketException
import kotlin.system.exitProcess

class App : Application() {

    companion object {
        const val TAG: String = "App"

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        @JvmStatic
        fun exit(activity: Activity? = null) {
            activity?.let { ActivityCompat.finishAffinity(it) }
            Process.killProcess(Process.myPid())
            exitProcess(0)
        }

        @JvmStatic
        fun restart(activity: Activity? = null,
            intent : Intent
        ) {
            activity?.let { ActivityCompat.finishAffinity(it) }
            context.startActivity(intent)
            exitProcess(0)
        }
    }

    private val databaseDebugUtil: DatabaseDebugUtil by inject()
    private val networkObserver: NetworkObserver by inject()

    override fun onCreate() {
        super.onCreate()
        initApp()
        initKoin()
        initMobileAds()
        initObserver()
        // rxJava 에러 제어
        setRxJavaError()
        // room 데이터베이스 디버그 활성화.
        debugDatabase()
    }

    /**
     * 앱 초기화.
     */
    private fun initApp() {
        context = this
    }

    /**
     * 코인 초기화.
     */
    private fun initKoin(): KoinApplication = startKoin {
        androidLogger()
        androidContext(androidContext = this@App)
        fragmentFactory()
        modules(AppModule.getModules())
    }

    /**
     * 옵저버 초기화.
     */
    private fun initObserver() {
        networkObserver.register()
    }

    /**
     * 구글 애드몹 초기화.
     */
    private fun initMobileAds() {
        MobileAds.initialize(this)
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun setRxJavaError() {
        RxJavaPlugins.setErrorHandler { e ->
            var error: Throwable? = e
            if (error is UndeliverableException) error = e.cause
            if (error is IOException || error is SocketException) return@setErrorHandler
            if (error is InterruptedException) return@setErrorHandler
            if (error is NullPointerException || error is IllegalArgumentException) {
                Thread.currentThread().uncaughtExceptionHandler.uncaughtException(
                    Thread.currentThread(), error
                )
                return@setErrorHandler
            }
            if (error is IllegalStateException) {
                Thread.currentThread().uncaughtExceptionHandler.uncaughtException(
                    Thread.currentThread(), error
                )
                return@setErrorHandler
            }
//            Log.d(TAG, "Undeliverable exception received, not sure what to do : $error")
        }
    }

    /**
     * 데이터 베이스 디버그.
     */
    private fun debugDatabase() = databaseDebugUtil.setCustomDatabaseFiles()
}