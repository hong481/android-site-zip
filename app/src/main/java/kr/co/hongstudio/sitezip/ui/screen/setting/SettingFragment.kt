package kr.co.hongstudio.sitezip.ui.screen.setting

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kr.co.hongstudio.sitezip.R
import kr.co.hongstudio.sitezip.base.fragment.BaseFragment
import kr.co.hongstudio.sitezip.data.local.preference.AppPreference
import kr.co.hongstudio.sitezip.data.local.preference.AppPreferenceImpl
import kr.co.hongstudio.sitezip.databinding.FragmentSettingBinding
import kr.co.hongstudio.sitezip.util.extension.observeBaseViewModelEvent
import org.koin.android.ext.android.inject
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

    private val appPref: AppPreference by inject()
    private val prefListener = OnSharedPreferenceChangeListener { _, key ->
        if (key == AppPreferenceImpl.Key.VISIBLE_APPIRATER_DIALOG) {
            viewModel.setVisibleAppiraterDialog(appPref.visibleAppiraterDialog)
        }
    }

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

        appPref.registerChangeListener(prefListener)

        viewModel.visibleAppiraterDialog.observe(viewLifecycleOwner, Observer {
            appPref.visibleAppiraterDialog = it
        })

        observeBaseViewModelEvent(viewModel)
    }

    override fun onDestroy() {
        super.onDestroy()
        appPref.unregisterChangeListener(prefListener)
    }
}