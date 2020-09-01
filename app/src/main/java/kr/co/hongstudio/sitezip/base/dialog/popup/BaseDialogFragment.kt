package kr.co.hongstudio.sitezip.base.dialog.popup

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.reactivex.disposables.CompositeDisposable
import kr.co.hongstudio.sitezip.util.ActivityUtil
import org.koin.android.ext.android.inject

abstract class BaseDialogFragment : AppCompatDialogFragment() {

    protected val window: Window?
        get() = dialog?.window

    protected var layoutParams: WindowManager.LayoutParams?
        get() {
            return window?.attributes
        }
        set(value) {
            window?.attributes = value
        }

    var onDismissListener: (() -> Unit)? = null

    protected val activityUtil: ActivityUtil by inject()

    protected val compositeDisposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = object : BaseAppcompatDialog(requireContext(), theme) {
        override fun onBackPressed() {
            if (!this@BaseDialogFragment.onBackPressed()) {
                return
            }
            super.onBackPressed()
        }
    }

    abstract override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        layoutParams = layoutParams?.apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount = 0.0f
        }
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
    }

    override fun show(manager: FragmentManager, tag: String?) = show(
        manager, tag, bundleOf()
    )

    fun show(manager: FragmentManager, tag: String?, args: Bundle) {
        if (!activityUtil.isForeground) {
            return
        }
        val dialog: Fragment? = manager.findFragmentByTag(tag)
        if (dialog != null && dialog.isAdded) {
            return
        }
        arguments = args
        super.show(manager, tag)
    }

    fun showAllowingStateLoss(manager: FragmentManager?, tag: String?, args: Bundle? = bundleOf()) {
        if (!activityUtil.isForeground) {
            return
        }
        val dialog: Fragment? = manager?.findFragmentByTag(tag)
        if (dialog != null && dialog.isAdded) {
            return
        }
        arguments = args
        manager?.beginTransaction()
            ?.add(this, tag)
            ?.commitAllowingStateLoss()
    }

    override fun dismiss() {
        if (!activityUtil.isForeground) {
            return
        }
        if (!isAdded) {
            return
        }
        super.dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (!activityUtil.isForeground) {
            return
        }
        if (!isAdded) {
            return
        }
        onDismissListener?.let {
            it()
        }
        super.onDismiss(dialog)
    }

    open fun onBackPressed(): Boolean {
        // empty method.
        return true
    }

}
