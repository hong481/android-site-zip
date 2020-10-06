package kr.co.hongstudio.sitezip.ui.appirater

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kr.co.hongstudio.sitezip.R
import kr.co.hongstudio.sitezip.base.dialog.popup.BaseDialogFragment
import kr.co.hongstudio.sitezip.databinding.DialogAppiraterBinding

class AppiraterDialog : BaseDialogFragment() {

    companion object {
        const val TAG: String = "AppiraterDialog"

        fun newInstance(): AppiraterDialog = AppiraterDialog().apply {
            arguments = Bundle()
        }
    }

    private val binding: DialogAppiraterBinding by lazy {
        DialogAppiraterBinding.bind(requireView())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.dialog_appirater, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initBinding()
    }

    private fun initBinding() {
        binding.lifecycleOwner = this
    }

}
