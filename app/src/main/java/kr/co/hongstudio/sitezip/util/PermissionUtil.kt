package kr.co.hongstudio.sitezip.util

import android.Manifest
import android.os.Build
import androidx.lifecycle.LifecycleOwner
import io.reactivex.rxjava3.disposables.Disposable
import kr.co.hongstudio.sitezip.util.extension.checkPermission

class PermissionUtil {

    companion object {
        const val TAG: String = "PermissionUtil"
    }

    /**
     * 유저 퍼미션 허가.
     */
    fun checkPermission(
        lifecycleOwner: LifecycleOwner,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ): Disposable =
        lifecycleOwner.checkPermission(
            permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ).run {
                toTypedArray()
            },
            onGranted = {
                onGranted()
            },
            onDenied = {
                onDenied()
            }
        )
}