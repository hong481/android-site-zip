package kr.co.hongstudio.sitezip.firebase

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kr.co.hongstudio.sitezip.data.BuildProperty
import kr.co.hongstudio.sitezip.util.extension.postValue
import java.util.*
import kotlin.collections.HashMap

class FireBaseDatabaseManager(

    buildProperty: BuildProperty

) {

    companion object {
        const val TAG: String = "FireBaseDatabaseManager"
        const val ZIPS_PATH = "zips"
        const val SITES_PATH = "sites"
        const val CONNECT_REF_PATH = ".info/connected"
    }

    private val _isAvailable: MutableLiveData<Boolean> = MutableLiveData()
    val isAvailable: LiveData<Boolean> = _isAvailable

    val database: FirebaseDatabase = Firebase.database

    val rootPath = "$ZIPS_PATH/${buildProperty.productName}/${Locale.getDefault().language}"

    val rootRef: DatabaseReference = database.getReference(rootPath)

    init {
        database.getReference(CONNECT_REF_PATH).apply {
            addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val connected = snapshot.getValue(Boolean::class.java)
                    connected?.let {
                        if (connected) {
                            Log.d(TAG, "connected")
                            _isAvailable.postValue = true
                        } else {
                            Log.d(TAG, "not connected")
                            _isAvailable.postValue = false
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Listener was cancelled")
                    _isAvailable.postValue = false
                }
            }
            )
        }
    }

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
        Log.d(TAG, "postData. refPath:$refPath / uniqueKey: $uniqueKey / data: $data")
        val updateMap = HashMap<String, Any>()
        updateMap[uniqueKey] = data
        database.getReference(refPath).updateChildren(updateMap)
    }

    /**
     * 루트 ref 리스너 제거.
     */
    fun removeRootRefListener(listener: ChildEventListener) {
        rootRef.removeEventListener(listener)
    }

}