package kr.co.hongstudio.sitezip.ui.screen.setting

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kr.co.hongstudio.sitezip.R
import kr.co.hongstudio.sitezip.base.fragment.BaseFragment
import kr.co.hongstudio.sitezip.databinding.FragmentSettingBinding
import kr.co.hongstudio.sitezip.util.extension.observeBaseViewModelEvent
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SettingFragment : BaseFragment() {

    companion object {
        const val TAG: String = "SettingFragment"

        fun newInstance(): SettingFragment = SettingFragment()
    }

    private val binding: FragmentSettingBinding by lazy {
        FragmentSettingBinding.bind(requireView())
    }

    val viewModel: SettingViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_setting, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initBinding()
        initViewModel()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart.")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart.")
    }

    private fun initViewModel() {
        Log.d(TAG, "initViewModel")
        observeBaseViewModelEvent(viewModel)
    }
}