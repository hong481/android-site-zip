package kr.co.hongstudio.sitezip.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kr.co.hongstudio.sitezip.R
import kr.co.hongstudio.sitezip.base.dialog.popup.BaseDialogFragment
import kr.co.hongstudio.sitezip.databinding.DialogProgressBinding
class ProgressDialog : BaseDialogFragment() {

    companion object {
        const val TAG: String = "ProgressDialog"

        fun newInstance(): ProgressDialog = ProgressDialog().apply {
            arguments = Bundle()
        }
    }

    private val binding: DialogProgressBinding by lazy {
        DialogProgressBinding.bind(requireView())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.dialog_progress, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initBinding()
    }

    private fun initBinding() {
        binding.lifecycleOwner = this
    }

}
