package kr.co.hongstudio.sitezip

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Process
import androidx.core.app.ActivityCompat
import com.google.android.gms.ads.MobileAds
import kr.co.hongstudio.sitezip.di.AppModule
import kr.co.hongstudio.sitezip.observer.NetworkObserver
import kr.co.hongstudio.sitezip.util.DatabaseDebugUtil
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.fragment.koin.fragmentFactory
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
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
    }

    private val databaseDebugUtil: DatabaseDebugUtil by inject()
    private val networkObserver: NetworkObserver by inject()

    override fun onCreate() {
        super.onCreate()
        initApp()
        initKoin()
        initMobileAds()
        initObserver()
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

    /**
     * 데이터 베이스 디버그.
     */
    private fun debugDatabase() = databaseDebugUtil.setCustomDatabaseFiles()
}