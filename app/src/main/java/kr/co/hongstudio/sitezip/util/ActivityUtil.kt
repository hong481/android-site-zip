package kr.co.hongstudio.sitezip.util

import android.app.ActivityManager
import android.content.Context

open class ActivityUtil(
    private val applicationContext: Context
) {

    protected val instance: ActivityManager =
        applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    /**
     * 앱이 Foreground 상태인지 체크
     *
     * @return true: foreground, false: background
     */
    open val isForeground: Boolean
        get() {
            for (processInfo: ActivityManager.RunningAppProcessInfo in instance.runningAppProcesses) {
                if (processInfo.processName != applicationContext.packageName) {
                    continue
                }
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return true
                }
            }
            return false
        }

}
