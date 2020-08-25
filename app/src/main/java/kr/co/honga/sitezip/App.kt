package kr.co.honga.sitezip

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Process
import androidx.core.app.ActivityCompat
import kr.co.honga.sitezip.di.AppModule
import kr.co.honga.sitezip.util.DatabaseDebugUtil
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.fragment.koin.fragmentFactory
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

    override fun onCreate() {
        super.onCreate()
        initApp()
        initKoin()

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
    private fun initKoin() = startKoin {
        androidLogger()
        androidContext(androidContext = this@App)
        fragmentFactory()
        modules(AppModule.getModules())
    }

    /**
     * 데이터 베이스 디버그.
     */
    private fun debugDatabase() = databaseDebugUtil.setCustomDatabaseFiles()
}