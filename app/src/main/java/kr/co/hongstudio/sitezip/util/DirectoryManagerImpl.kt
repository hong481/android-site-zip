package kr.co.hongstudio.sitezip.util

import android.content.Context
import java.io.File

class DirectoryManagerImpl(

    private val applicationContext: Context,
    private val fileUtil: FileUtil

) : DirectoryManager {

    companion object {
        const val LOG_DIR: String = "log"
        const val DOWNLOAD_DIR: String = "download"
        const val OKHTTP_DIR: String = "okhttp"
        const val DATABASE_DIR: String = "database"
    }

    /**
     * root dir.
     */
    override val rootDir: File
        get() = obtainRootDir()

    /**
     * {root}/cache dir.
     */
    override val cacheDir: File
        get() = obtainCacheDir()

    /**
     * {root}/files/log dir.
     */
    override val logDir: File
        get() = obtainLogDir()

    /**
     * {root}/files/database dir.
     */
    val databaseDir: File
        get() = obtainDatabaseDir()


    /**
     * Root 폴더 리턴.
     */
    private fun obtainRootDir(): File = (applicationContext.getExternalFilesDir(null)
        ?: applicationContext.filesDir).also { rootDir ->
        fileUtil.createDirectory(rootDir)
    }


    /**
     * Cache 폴더 리턴.
     */
    private fun obtainCacheDir(): File = (applicationContext.externalCacheDir
        ?: applicationContext.cacheDir).also { cache ->
        fileUtil.createDirectory(cache)
    }

    /**
     * log 폴더 리턴.
     */
    private fun obtainLogDir(): File = File(
        rootDir,
        LOG_DIR
    ).also { logDir ->
        fileUtil.createDirectory(logDir)
    }


    /**
     * Download 폴더 리턴.
     */
    private fun obtainDownloadDir(): File = File(
        rootDir,
        DOWNLOAD_DIR
    ).also { downloadDir ->
        fileUtil.createDirectory(downloadDir)
    }

    /**
     * Okhttp 폴더 리턴.
     */
    private fun obtainOkHttpDir(): File = File(
        cacheDir,
        OKHTTP_DIR
    ).also { okHttpDir ->
        fileUtil.createDirectory(okHttpDir)
    }

    /**
     * Database 폴더 리턴.
     */
    private fun obtainDatabaseDir(): File = File(
        rootDir,
        DATABASE_DIR
    ).also { databaseDir ->
        fileUtil.createDirectory(databaseDir)
    }
}

interface DirectoryManager {

    /**
     * 루트 저장소.
     */
    val rootDir: File

    /**
     * 캐시 저장소.
     */
    val cacheDir: File

    /**
     * 로그 파일 저장소.
     */
    val logDir: File
}