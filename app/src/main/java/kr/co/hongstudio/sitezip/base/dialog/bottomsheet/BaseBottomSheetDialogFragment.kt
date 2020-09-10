package kr.co.hongstudio.sitezip.base.dialog.bottomsheet

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.disposables.CompositeDisposable
import kr.co.hongstudio.sitezip.util.ActivityUtil
import kr.co.irlink.irsdk.util.base.dialog.bottomsheet.BaseBottomSheetDialog
import org.koin.android.ext.android.inject

abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    object Extra {
        const val STATE: String = "BaseBottomSheetDialogFragment.Extra.STATE"
        const val IS_HIDEABLE: String = "BaseBottomSheetDialogFragment.Extra.IS_HIDEABLE"
        const val PEEK_HEIGHT: String = "BaseBottomSheetDialogFragment.Extra.PEEK_HEIGHT"
        const val DIM_AMOUNT: String = "BaseBottomSheetDialogFragment.Extra.DIM_AMOUNT"
    }

    protected val rootView: ViewGroup
        get() = view?.parent as ViewGroup

    protected val bottomSheetBehavior: BottomSheetBehavior<*> by lazy {
        val lp: CoordinatorLayout.LayoutParams =
            rootView.layoutParams as CoordinatorLayout.LayoutParams

        return@lazy lp.behavior as BottomSheetBehavior<*>
    }

    protected var windowAttributes: WindowManager.LayoutParams?
        set(value) {
            dialog?.window?.attributes = value
        }
        get() = dialog?.window?.attributes

    var state: Int
        set(value) {
            bottomSheetBehavior.state = value
        }
        get() = bottomSheetBehavior.state

    var peekHeight: Int
        set(value) {
            bottomSheetBehavior.peekHeight = value
        }
        get() = bottomSheetBehavior.peekHeight

    var isHideable: Boolean
        set(value) {
            bottomSheetBehavior.isHideable = value
        }
        get() = bottomSheetBehavior.isHideable

    var dimAmount: Float
        set(value) {
            windowAttributes?.dimAmount = value
        }
        get() = windowAttributes?.dimAmount ?: 0.0f

    var onSlideListener: ((bottomSheet: View, slideOffset: Float) -> Unit)? = null

    var onStateChangedListener: ((bottomSheet: View, newState: Int) -> Unit)? = null

    var onDismissListener: (() -> Unit)? = null

    protected val activityUtil: ActivityUtil by inject()

    protected val compositeDisposable: CompositeDisposable = CompositeDisposable()

    protected val bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback =
        object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                onSlideListener?.let {
                    it(bottomSheet, slideOffset)
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> dismiss()
                    BottomSheetBehavior.STATE_DRAGGING -> Unit
                    BottomSheetBehavior.STATE_EXPANDED -> Unit
                    BottomSheetBehavior.STATE_SETTLING -> Unit
                    BottomSheetBehavior.STATE_COLLAPSED -> Unit
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> Unit
                }
                onStateChangedListener?.let {
                    it(bottomSheet, newState)
                }
            }
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        object : BaseBottomSheetDialog(requireContext(), theme) {
            override fun onBackPressed() {
                if (!this@BaseBottomSheetDialogFragment.onBackPressed()) {
                    return
                }
                super.onBackPressed()
            }
        }

    abstract override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        rootView.background = null

        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        val instanceState = savedInstanceState ?: arguments ?: Bundle().also {
            arguments = it
        }
        onSetupInstanceState(instanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
        compositeDisposable.clear()
        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
    }

    @CallSuper
    private fun onSetupInstanceState(savedInstanceState: Bundle) {
        isHideable = savedInstanceState.getBoolean(Extra.IS_HIDEABLE, true)
        state = savedInstanceState.getInt(Extra.STATE, BottomSheetBehavior.STATE_COLLAPSED)
        peekHeight =
            savedInstanceState.getInt(Extra.PEEK_HEIGHT, BottomSheetBehavior.PEEK_HEIGHT_AUTO)
        dimAmount = savedInstanceState.getFloat(Extra.DIM_AMOUNT, 0.6f)
    }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(Extra.STATE, state)
        outState.putInt(Extra.PEEK_HEIGHT, peekHeight)
        outState.putBoolean(Extra.IS_HIDEABLE, isHideable)
        outState.putFloat(Extra.DIM_AMOUNT, dimAmount)
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
