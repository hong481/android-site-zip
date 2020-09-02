package kr.co.hongstudio.sitezip.firebase

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kr.co.hongstudio.sitezip.util.extension.postValue

class FireBaseDatabaseManager {

    companion object {
        const val TAG: String = "FireBaseDatabaseManager"
        const val SITES_PATH = "sites"
        const val CONNECT_REF_PATH = ".info/connected"
    }

    private val _isAvailable: MutableLiveData<Boolean> = MutableLiveData()
    val isAvailable: LiveData<Boolean> = _isAvailable

    val database = Firebase.database

    init {
        database.getReference(CONNECT_REF_PATH).apply {
            addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                   val connected  = snapshot.getValue(Boolean::class.java)
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

}