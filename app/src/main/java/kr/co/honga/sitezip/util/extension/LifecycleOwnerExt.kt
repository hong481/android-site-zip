package kr.co.honga.sitezip.util.extension

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import kr.co.honga.sitezip.base.dialog.bottomsheet.BaseBottomSheetDialogFragment
import kr.co.honga.sitezip.base.dialog.popup.BaseDialogFragment
import kr.co.honga.sitezip.base.livedata.EventObserver
import kr.co.honga.sitezip.base.viewmodel.BaseViewModel
import kr.co.honga.sitezip.ui.progress.ProgressDialog

/**
 * Context를 리턴.
 */
val LifecycleOwner.lifecycleContext: Context
    get() = when (this) {
        is Activity -> this
        is Fragment -> this.context ?: throw NullPointerException("The context of the fragment is null.")
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