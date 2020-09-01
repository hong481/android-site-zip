package kr.co.hongstudio.sitezip.util.extension

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.tedpark.tedpermission.rx2.TedRx2Permission
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.hongstudio.sitezip.R
import kr.co.hongstudio.sitezip.base.dialog.bottomsheet.BaseBottomSheetDialogFragment
import kr.co.hongstudio.sitezip.base.dialog.popup.BaseDialogFragment
import kr.co.hongstudio.sitezip.base.livedata.EventObserver
import kr.co.hongstudio.sitezip.base.viewmodel.BaseViewModel
import kr.co.hongstudio.sitezip.ui.progress.ProgressDialog

/**
 * Context를 리턴.
 */
val LifecycleOwner.lifecycleContext: Context
    get() = when (this) {
        is Activity -> this
        is Fragment -> this.context
            ?: throw NullPointerException("The context of the fragment is null.")
        else -> throw NullPointerException("This method can only use Activity or Fragment.")
    }

/**
 * FragmentManager를 리턴.
 */
val LifecycleOwner.lifecycleFragmentManager: FragmentManager
    get() = when (this) {
        is AppCompatActivity -> this.supportFragmentManager
        is Fragment -> this.childFragmentManager
        else -> throw NullPointerException("This method can only use Activity or Fragment.")
    }

/**
 * 퍼미션 체크.
 */
fun LifecycleOwner.checkPermission(
    permissions: Array<String>,
    onGranted: (() -> Unit)?,
    onDenied: (() -> Unit)? = null
): Disposable = TedRx2Permission.with(lifecycleContext)

    // 퍼미션이 필요한 이유 설명
    // .setRationaleMessage(context.getString(R.string.permission_rational_message))
    // .setRationaleConfirmText(context.getString(R.string.common_ok))

    // 퍼미션 요청을 거절한 경우 보이는 메세지
    .setDeniedMessage(lifecycleContext.getString(R.string.permission_denied_message))
    .setDeniedCloseButtonText(lifecycleContext.getString(R.string.close))

    // 퍼미션을 거절한 경우 설정 화면으로 바로 이동 버튼
    .setGotoSettingButton(true)
    .setGotoSettingButtonText(lifecycleContext.getString(R.string.setting))

    // 권한을 획득할 퍼미션
    .setPermissions(*permissions)

    .request()
    .toV3()

    // Thread 설정
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())

    // 구독
    .subscribe({ tedPermissionResult ->
        when (tedPermissionResult.isGranted) {
            true -> onGranted?.let {
                it()
            }
            false -> onDenied?.let {
                it()

            } ?: let {
                Toast.makeText(
                    lifecycleContext,
                    R.string.permission_denied_message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }, {
        it.printStackTrace()
    })

/**
 *  BaseViewModel의 기본 Event 구독.
 */
fun LifecycleOwner.observeBaseViewModelEvent(
    viewModel: BaseViewModel,
    isShowToast: Boolean = true,
    isShowProgress: Boolean = true,
    isDismissProgress: Boolean = true
) {
    if (isShowToast) {
        viewModel.showToast.observe(this, EventObserver { message ->
            Toast.makeText(lifecycleContext, message, Toast.LENGTH_SHORT).show()
        })
    }
    if (isShowProgress) {
        viewModel.showProgress.observe(this, EventObserver {
            showProgressDialog()
        })
    }
    if (isDismissProgress) {
        viewModel.dismissProgress.observe(this, EventObserver {
            dismissProgressDialog()
        })
    }
}

/**
 * ProgressDialog 띄우기.
 */
fun LifecycleOwner.showProgressDialog() = ProgressDialog.newInstance()
    .show(lifecycleFragmentManager, ProgressDialog.TAG)

/**
 * ProgressDialog 닫기.
 */
fun LifecycleOwner.dismissProgressDialog() {
    val dialog: Fragment? = lifecycleFragmentManager.findFragmentByTag(ProgressDialog.TAG)
    if (dialog != null) {
        dismissDialog(ProgressDialog.TAG)
    } else {
        timer(500) {
            dismissDialog(ProgressDialog.TAG)
        }
    }
}

/**
 * DialogFragment 닫기.
 */
fun LifecycleOwner.dismissDialog(dialogTag: String) {
    lifecycleFragmentManager.findFragmentByTag(dialogTag).let { dialog ->
        when (dialog) {
            is BaseDialogFragment -> dialog.dismiss()
            is BaseBottomSheetDialogFragment -> dialog.dismiss()
        }
    }
}