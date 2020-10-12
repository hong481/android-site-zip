package kr.co.hongstudio.sitezip.ui.appirater

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kr.co.hongstudio.sitezip.R
import kr.co.hongstudio.sitezip.base.dialog.popup.BaseDialogFragment
import kr.co.hongstudio.sitezip.base.livedata.EventObserver
import kr.co.hongstudio.sitezip.databinding.DialogAppiraterBinding
import kr.co.hongstudio.sitezip.googleplay.InAppReviewManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AppiraterDialog : BaseDialogFragment() {

    companion object {
        const val TAG: String = "AppiraterDialog"

        fun newInstance(callback: AppiraterDialogCallback? = null): AppiraterDialog =
            AppiraterDialog().apply {
                arguments = Bundle()
                this.callback = callback
            }
    }

    private val viewModel: AppiraterDialogViewModel by sharedViewModel()

    private val inAppReviewManager: InAppReviewManager by inject()
    private var callback: AppiraterDialogCallback? = null

    private val binding: DialogAppiraterBinding by lazy {
        DialogAppiraterBinding.bind(requireView())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_appirater, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initDialog()
        initBinding()
        initViewModel()
    }

    private fun initDialog() {
        dialog?.setCancelable(false)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
    }

    private fun initBinding() {
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }

    private fun initViewModel() {
        viewModel.isVisibleDialog.observe(viewLifecycleOwner, Observer {
            viewModel.setVisibleDialog(!it)
        })
        viewModel.appReviewStartEvent.observe(viewLifecycleOwner, EventObserver { baseUrl ->
            try {
                inAppReviewManager.showInAppReview(
                    activity = activity as Activity,
                    onComplete = {},
                    onFail = {
                        val fullUrl = "$baseUrl${context?.packageName ?: return@showInAppReview}"
                        val actionIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                        startActivity(actionIntent)
                    }
                )
                dismiss()
                callback?.onDismiss()
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        })
        viewModel.closeDialogEvent.observe(viewLifecycleOwner, EventObserver {
            dismiss()
            callback?.onDismiss()
        })
    }

    interface AppiraterDialogCallback {
        fun onDismiss()
    }
}
