package kr.co.hongstudio.sitezip.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.util.*

open class FileUtil {

    companion object {
        const val TAG: String = "FileUtil"
    }

    /**
     * 파일 이동
     */
    open fun moveFile(targetFile: File, moveFile: File): Boolean = try {
        targetFile.renameTo(moveFile).also { isRenameTo ->
            Log.d(TAG, "movedFile: ${targetFile.path} to ${moveFile.path} [$isRenameTo]")
        }
    } catch (e: Exception) {
        Log.d(TAG, e.toString())
        false
    }

    /**
     * 파일 삭제
     */
    open fun deleteFile(file: File): Boolean = try {
        if (file.exists())
            file.delete().also { isDeleted ->
                Log.d(TAG, "deleteFile: ${file.path} [$isDeleted]")
            }
        else
            false
    } catch (e: Exception) {
        Log.d(TAG, e.toString())
        false
    }

    /**
     * 파일 생성
     */
    open fun createFile(file: File): Boolean = try {
        if (!file.exists()) {
            file.createNewFile().also { isCreated ->
                Log.d(TAG, "createFile: ${file.path} [$isCreated]")
            }
        } else {
            true
        }
    } catch (e: Exception) {
        Log.d(TAG, e.toString())
        false
    }

    /**
     * 폴더 생성
     */
    open fun createDirectory(directory: File): Boolean = try {
        if (!directory.exists()) {
            directory.mkdirs().also { isCreated ->
                Log.d(TAG, "createDirectory: ${directory.path} [$isCreated]")
            }
        } else {
            true
        }
    } catch (e: Exception) {
        Log.d(TAG, e.toString())
        false
    }

    /**
     * 파일명 변경
     */
    open fun renameFile(fromFile: File, rename: String): Boolean = renameFile(
        fromFile = fromFile,
        toFile = File(fromFile.parentFile, rename)
    )

    /**
     * 파일명 변경
     */
    open fun renameFile(fromFile: File, toFile: File): Boolean = try {
        fromFile.renameTo(toFile).also { isRenamed ->
            Log.d(TAG, "renameFile: ${fromFile.name} to ${toFile.name} [$isRenamed]")
        }
    } catch (e: Exception) {
        Log.d(TAG, e.toString())
        false
    }

    /**
     * 폴더내 파일 리스트 이동
     */
    open fun moveFileList(targetDir: File, moveDir: File): Boolean = try {
        if (targetDir.exists()) {
            val files = targetDir.listFiles()
            files?.forEach { file ->
                val fileToMove = File(moveDir, file.name)
                file.renameTo(fileToMove)
            }
            true
        } else {
            false
        }
    } catch (e: Exception) {
        Log.d(TAG, e.toString())
        false
    }

    /**
     * 폴더내 파일 리스트 정보 리턴
     */
    open fun getFileListInDirectory(directory: File): StringBuffer {
        val files = directory.listFiles()
        val list = StringBuffer()

        files?.forEach { file ->
            list.append(file.name)
        }
        return list
    }

}

/**
 * File to Uri.
 */
fun File.toUri(): Uri = Uri.fromFile(this)

/**
 * File to Uri.
 * 단 외부에 공개 가능한 Uri 형태로 리턴된다.
 */
fun File.toProvideUri(context: Context): Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", this)
} else {
    toUri()
}

/**
 * 파일의 확장자 리턴.
 */
val String.extension: String
    get() = substringAfterLast('.', "")

/**
 * 파일의 확장자를 제외한 파일명 리턴.
 */
val String.nameWithoutExtension: String
    get() = substringBeforeLast(".")

/**
 * 파일의 생성일자를 리턴.
 */
val File.lastModifiedDate: Date
    get() = Date(lastModified())