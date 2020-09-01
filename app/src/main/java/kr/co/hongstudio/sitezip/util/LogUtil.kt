package kr.co.hongstudio.sitezip.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import kr.co.hongstudio.sitezip.util.extension.format
import kr.co.hongstudio.sitezip.util.extension.synchronizedThread
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.*
import java.util.*

object LogUtil : KoinComponent {

    const val TAG: String = "LogUtil"

    const val MAX_FILE_SIZE: Long = 1024 * 1024 * 2 // 2MB

    private val applicationContext: Context by inject()

    private val directoryManager: DirectoryManager by inject()

    /**
     * 읽기, 쓰기 권한 허용 여부.
     */
    private val isGrantedPermissions: Boolean
        get() {
            val isReadPermission = ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            val isWritePermission = ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            return isReadPermission && isWritePermission
        }

    /**
     * Verbose 로그.
     */
    fun v(tag: String, message: String, isWrite: Boolean = true) = printLog(
        tag = tag,
        message = message,
        isWrite = isWrite,
        level = Log.VERBOSE
    )

    /**
     * Debug 로그.
     */
    fun d(tag: String, message: String, isWrite: Boolean = true) = printLog(
        tag = tag,
        message = message,
        isWrite = isWrite,
        level = Log.DEBUG
    )

    /**
     * Info 로그.
     */
    fun i(tag: String, message: String, isWrite: Boolean = true) = printLog(
        tag = tag,
        message = message,
        isWrite = isWrite,
        level = Log.INFO
    )

    /**
     * Warn 로그.
     */
    fun w(tag: String, message: String, isWrite: Boolean = true) = printLog(
        tag = tag,
        message = message,
        isWrite = isWrite,
        level = Log.WARN
    )

    /**
     * Error 로그.
     */
    fun e(tag: String, message: String, isWrite: Boolean = true) = printLog(
        tag = tag,
        message = message,
        isWrite = isWrite,
        level = Log.ERROR
    )

    /**
     * 로그 출력 및 파일 저장.
     */
    fun printLog(tag: String, message: String, isWrite: Boolean, level: Int) {
        if (message.isEmpty()) {
            return
        }
        when (level) {
            Log.VERBOSE -> Log.v(tag, message)
            Log.DEBUG -> Log.d(tag, message)
            Log.INFO -> Log.i(tag, message)
            Log.WARN -> Log.w(tag, message)
            Log.ERROR -> Log.e(tag, message)
        }
        if (isWrite) {
            writeToFile(tag, message)
        }
    }

    /**
     * Exception 로그 출력 및 파일 저장.
     */
    fun exception(tag: String, t: Throwable) {
        val error = StringWriter().also {
            t.printStackTrace()
            t.printStackTrace(PrintWriter(it))
        }
        writeToFile(tag, error.toString())
    }

    /**
     * 로그 텍스트 파일 저장.
     */
    fun writeToFile(tag: String, message: String) = synchronizedThread(this) {
        if (!isGrantedPermissions) {
            return@synchronizedThread
        }
        try {
            if (message.isEmpty()) {
                return@synchronizedThread
            }
            val fileName = "log_${Date() format "yyyyMMdd"}"
            val dateNow = Date() format "MM-dd HH:mm:ss.SSS"

            var logFile: File
            var fileCount: Int = 0

            while (true) {
                logFile = if (fileCount == 0) {
                    File(directoryManager.logDir, "$fileName.txt")
                } else {
                    File(directoryManager.logDir, "$fileName[$fileCount].txt")
                }
                if (!logFile.exists()) {
                    logFile.createNewFile()
                }
                if (logFile.length() >= MAX_FILE_SIZE) {
                    fileCount++
                    continue
                }
                break
            }
            BufferedWriter(FileWriter(logFile, true)).apply {
                write(String.format("[%1s] [%2s]: %3s\r\n", dateNow, tag, message))
                close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}