package kr.co.hongstudio.sitezip.ui.screen.place

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import kr.co.hongstudio.sitezip.R
import kr.co.hongstudio.sitezip.base.fragment.BaseFragment
import kr.co.hongstudio.sitezip.base.livedata.EventObserver
import kr.co.hongstudio.sitezip.base.model.Model
import kr.co.hongstudio.sitezip.data.local.entity.PlaceZip
import kr.co.hongstudio.sitezip.databinding.FragmentPlaceZipBinding
import kr.co.hongstudio.sitezip.ui.screen.place.PlaceZipViewModel.Serializable.PLACE_ZIP
import kr.co.hongstudio.sitezip.util.PermissionUtil
import kr.co.hongstudio.sitezip.util.extension.observeBaseViewModelEvent
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getStateViewModel

class PlaceZipFragment : BaseFragment() {

    companion object {
        const val TAG: String = "PlaceListFragment"

        fun newInstance(placeZip: PlaceZip): PlaceZipFragment =
            PlaceZipFragment().apply {
                arguments = bundleOf()
                arguments?.putParcelable(PLACE_ZIP, placeZip)
            }
    }

    private val binding: FragmentPlaceZipBinding by lazy {
        FragmentPlaceZipBinding.bind(requireView())
    }

    val viewModel: PlaceZipViewModel by lazy {
        getStateViewModel<PlaceZipViewModel>(bundle = arguments)
    }

    private val permissionUtil: PermissionUtil by inject()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_place_zip, container, true)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initBinding()
        initViewModel()
    }


    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
    }

    private fun initViewModel() {
        viewModel.placeZip.observe(viewLifecycleOwner, Observer { place ->
            if (place.state == Model.TRUE) {
                // 권한 요청
                permissionUtil.checkPermission(this,
                    onGranted = {
                        viewModel.setPermissionGranted(true)
                    },
                    onDenied = {
                        viewModel.setPermissionGranted(false)
                    }
                )
            }
        })
        viewModel.location.observe(viewLifecycleOwner, Observer { location ->
            viewModel.setAddress(location)
        })
        viewModel.permissionGranted.observe(viewLifecycleOwner, EventObserver {
            if (it) {
                viewModel.registerLocationCallback()
            } else {
                viewModel.unregisterLocationCallback()
            }
        })
        observeBaseViewModelEvent(viewModel)
    }
}