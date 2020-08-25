package kr.co.honga.sitezip.util

import android.util.Log
import kr.co.honga.sitezip.BuildConfig
import java.io.File
import java.lang.reflect.Method

class DatabaseDebugUtil(

    private val dataBaseFile: File

) {

    companion object {
        const val TAG: String = "DatabaseDebugUtil"

        const val DEBUG_DATABASE_CLASS = "com.amitshekhar.DebugDB"
        const val DEBUG_DATABASE_METHOD = "setCustomDatabaseFiles"
    }

    /**
     * 커스텀 데이터 베이스 디버그.
     */
    fun setCustomDatabaseFiles() {
        if (BuildConfig.DEBUG) {
            try {
                val debugDB = Class.forName(DEBUG_DATABASE_CLASS)
                val argTypes = arrayOf<Class<*>>(HashMap::class.java)
                val setCustomDatabaseFiles: Method =
                    debugDB.getMethod(DEBUG_DATABASE_METHOD, *argTypes)
                val customDatabaseFiles: HashMap<String, android.util.Pair<File, String>> =
                    HashMap()
                customDatabaseFiles[dataBaseFile.name] = android.util.Pair(dataBaseFile, "")
                LogUtil.d(
                    TAG,
                    "setCustomDatabaseFiles." +
                            "\ndataBaseFile.name: ${dataBaseFile.name}" +
                            "\ndataBaseFile.absolutePath: ${dataBaseFile.absolutePath}"
                )
                setCustomDatabaseFiles.invoke(null, customDatabaseFiles)
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
            }
        }
    }
}