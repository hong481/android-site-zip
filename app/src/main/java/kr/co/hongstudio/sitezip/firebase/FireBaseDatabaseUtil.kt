package kr.co.hongstudio.sitezip.firebase

import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FireBaseDatabaseUtil {

    companion object {
        const val TAG: String = "FireBaseDatabaseUtil"
        const val SITES_PATH = "sites"
    }

    val database = Firebase.database

    /**
     * PUSH.
     */
    fun pushData(
        refPath: String,
        data: Any,
        onComplete: ((uniqueKey: String?) -> Unit)? = null
    ) {
        Log.d(TAG, "postData. refPath:$refPath / data: $data")
        val pushedPostRef: DatabaseReference = database.getReference(refPath).push()
        pushedPostRef.setValue(data)
        onComplete?.let {
            it(pushedPostRef.key)
        }
    }

    /**
     * UPDATE.
     */
    fun updateData(refPath: String, uniqueKey: String, data: Any) {
        Log.d(TAG, "postData. refPath:$refPath / uniiqueKey: $uniqueKey / data: $data")
        val updateMap = HashMap<String, Any>()
        updateMap[uniqueKey] = data
        database.getReference(refPath).updateChildren(updateMap)
    }

}