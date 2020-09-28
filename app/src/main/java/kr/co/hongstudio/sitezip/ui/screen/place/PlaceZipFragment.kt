package kr.co.hongstudio.sitezip.ui.screen.place

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.*
import android.widget.AbsListView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.co.hongstudio.sitezip.R
import kr.co.hongstudio.sitezip.base.fragment.BaseFragment
import kr.co.hongstudio.sitezip.base.livedata.EventObserver
import kr.co.hongstudio.sitezip.base.model.Model
import kr.co.hongstudio.sitezip.data.local.entity.PlaceZip
import kr.co.hongstudio.sitezip.databinding.FragmentPlaceZipBinding
import kr.co.hongstudio.sitezip.ui.screen.MainViewModel
import kr.co.hongstudio.sitezip.ui.screen.OuterActivities
import kr.co.hongstudio.sitezip.ui.screen.place.PlaceZipViewModel.Serializable.PLACE_ZIP
import kr.co.hongstudio.sitezip.util.LogUtil
import kr.co.hongstudio.sitezip.util.PermissionUtil
import kr.co.hongstudio.sitezip.util.ResourceProvider
import kr.co.hongstudio.sitezip.util.extension.observeBaseViewModelEvent
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getStateViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

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

    private val mainViewModel: MainViewModel by sharedViewModel()

    private val permissionUtil: PermissionUtil by inject()

    private val resourceProvider: ResourceProvider by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_place_zip, container, true)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initBinding()
        initViewModel()
        initPlacesRecyclerView()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.rvPlaces.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    AbsListView.OnScrollListener.SCROLL_STATE_FLING -> {
                        mainViewModel.setViewPagerUserInputEnabled(false)
                    }
                    AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL -> {
                        mainViewModel.setViewPagerUserInputEnabled(false)
                    }
                    AbsListView.OnScrollListener.SCROLL_STATE_IDLE -> {
                        mainViewModel.setViewPagerUserInputEnabled(true)
                    }
                    else -> Unit
                }
            }
        })
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart.")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart.")
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.setSearchText("")
        mainViewModel.setSearchVisibility(false)
        mainViewModel.setSearchButtonVisible(false)
        mainViewModel.setFavoriteButtonVisible(false)
        if (!viewModel.locationUtil.checkLocationServicesStatus()) {
            showLocationServiceSetting()
        }
    }

    private fun initViewModel() {
        binding.etSearchText.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                KeyEvent.KEYCODE_SEARCH -> viewModel.getPlaces(
                    viewModel.searchText.value ?: return@setOnEditorActionListener false
                )
            }
            true
        }
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
        viewModel.address.observe(viewLifecycleOwner, Observer { address ->
            Log.d(TAG, "address: $address")
            val query = "${address.thoroughfare} ${viewModel.placeZip.value?.defaultQuery}"
            viewModel.getInitPlaces(query)
        })
        viewModel.permissionGranted.observe(viewLifecycleOwner, EventObserver {
            if (it) {
                viewModel.registerLocationCallback()
            } else {
                viewModel.unregisterLocationCallback()
            }
        })
        viewModel.searchText.observe(viewLifecycleOwner, Observer {
            LogUtil.d(TAG, "viewModel.searchText.observe.")
            viewModel.searchSites()
        })
        viewModel.searchTextSelection.observe(viewLifecycleOwner, EventObserver {
            binding.etSearchText.setSelection(binding.etSearchText.length())
        })
        viewModel.playVoiceSearch.observe(viewLifecycleOwner, EventObserver {
            OuterActivities.intentVoiceSearch(context = context ?: return@EventObserver) {
                if (it.data != null) {
                    val textArray: ArrayList<String>? =
                        it.data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    textArray.let {
                        viewModel.setSearchText(textArray?.get(0) ?: "", true)
                    }
                }
            }
        })
        viewModel.intentUrlEvent.observe(viewLifecycleOwner, EventObserver { url ->
            try {
                val actionIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(actionIntent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        })
        observeBaseViewModelEvent(viewModel)
    }

    /**
     * 장소 리사이클 뷰 초기화.
     */
    private fun initPlacesRecyclerView() {
        binding.rvPlaces.setHasFixedSize(true)
        binding.rvPlaces.itemAnimator = null
        binding.rvPlaces.layoutManager = LinearLayoutManager(activity)
        binding.rvPlaces.adapter = PlacesAdapter(this, viewModel)
    }

    /**
     * 위치 서비스 설정 팝업창.
     */
    private fun showLocationServiceSetting() = AlertDialog.Builder(context)
        .setTitle(resourceProvider.getString(R.string.location_service_activation_title))
        .setMessage(resourceProvider.getString(R.string.location_service_activation_message))
        .setPositiveButton(resourceProvider.getString(R.string.setting)) { _, _ ->
            OuterActivities.intentGpsSetting(context = context ?: return@setPositiveButton)
        }
        .setNegativeButton(resourceProvider.getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }
        .create()
        .show()
}