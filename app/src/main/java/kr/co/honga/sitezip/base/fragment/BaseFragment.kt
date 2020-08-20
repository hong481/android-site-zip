package kr.co.honga.sitezip.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.reactivex.disposables.CompositeDisposable

abstract class BaseFragment : Fragment() {

    private var isRestart: Boolean = false

    protected val compositeDisposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    abstract override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View

    override fun onStart() {
        if (isRestart) {
            onRestart()
        }
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        if (!isRestart) {
            isRestart = true
        }
    }

    open fun onRestart() {
        // empty..
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isRestart) {
            isRestart = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    fun replace(fragmentManager: FragmentManager, container: View, tag: String, args: Bundle = bundleOf()) {
        fragmentManager.beginTransaction()
            .replace(container.id, this.apply { arguments = args }, tag)
            .commit()
    }

    fun add(fragmentManager: FragmentManager, container: View, tag: String, args: Bundle = bundleOf()) {
        if (isAdded) {
            return
        }
        fragmentManager.beginTransaction()
            .add(container.id, this.apply { arguments = args }, tag)
            .commit()
    }

    fun remove() {
        if (!isAdded) {
            return
        }
        parentFragmentManager.beginTransaction()
            .remove(this)
            .commit()
    }

    fun attach(fragmentManager: FragmentManager, container: View, tag: String, args: Bundle = bundleOf()) {
        if (!isAdded) {
            add(fragmentManager, container, tag, args)
            return
        }
        if (!isDetached) {
            return
        }
        fragmentManager.beginTransaction()
            .attach(this)
            .commit()
    }

    fun detach() {
        if (!isAdded || isDetached) {
            return
        }
        parentFragmentManager.beginTransaction()
            .detach(this)
            .commit()
    }

    fun show(fragmentManager: FragmentManager, container: View, tag: String, args: Bundle = bundleOf()) {
        if (!isAdded) {
            add(fragmentManager, container, tag, args)
            return
        }
        if (isVisible) {
            return
        }
        fragmentManager.beginTransaction()
            .show(this)
            .commit()
    }

    fun hide() {
        if (!isAdded || !isVisible) {
            return
        }
        parentFragmentManager.beginTransaction()
            .hide(this)
            .commit()
    }

    fun finish() {
        activity?.finish()
    }

}
