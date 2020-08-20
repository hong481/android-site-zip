package kr.co.honga.sitezip.firebase

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.File
import java.io.FileInputStream

class FireBaseStorageUtil {

    private val fireBaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

    /**
     * 파일 업로드.
     */
    fun uploadFile(
        uploadName: String,
        targetFile: File,
        onSuccess: ((taskSnapshot : UploadTask.TaskSnapshot) -> Unit),
        onFail: (() -> Unit)
    ) {
        val storageRef = fireBaseStorage.reference
        val mountainsRef = storageRef.child(uploadName)
        val fileStream = FileInputStream(targetFile)
        mountainsRef.putStream(fileStream).apply {
            addOnSuccessListener {
                onSuccess(it)
            }
            addOnCanceledListener {
                onFail()
            }
        }
    }
}