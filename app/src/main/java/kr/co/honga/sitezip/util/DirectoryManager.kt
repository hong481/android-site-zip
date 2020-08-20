package kr.co.honga.sitezip.util

import java.io.File

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

    /**
     * 레코드 라이브러리 로그 파일 저장소.
     */
    val recordLogDir: File

    /**
     * 다운로드 파일 저장소.
     */
    val downloadDir: File

    /**
     * 레코드 파일 저장소.
     */
    val recordDir: File

    /**
     * 암호화 파일 저장소.
     */
    val encDir: File

    /**
     * 복호화 파일 저장소.
     */
    val decDir: File

    /**
     * 사콜 저장소.
     */
    val saCallDir: File

    /**
     * 백업 파일 저장소.
     */
    val backupDir: File

    /**
     * 전송 실패 파일 저장소.
     */
    val failDir: File

    /**
     * OkHttp 캐시 저장소.
     */
    val okHttpDir: File

    /**
     * @see recordDir 폴더명 변경.
     */
    fun renameRecordDir(name: String)
}