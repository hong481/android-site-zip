package kr.co.hongstudio.sitezip.ui.screen.place

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import kr.co.hongstudio.sitezip.R
import kr.co.hongstudio.sitezip.base.fragment.BaseFragment
import kr.co.hongstudio.sitezip.base.livedata.EventObserver
import kr.co.hongstudio.sitezip.base.model.Model
import kr.co.hongstudio.sitezip.data.local.entity.PlaceZip
import kr.co.hongstudio.sitezip.databinding.FragmentPlaceZipBinding
import kr.co.hongstudio.sitezip.ui.screen.OuterActivities
import kr.co.hongstudio.sitezip.util.KeyboardUtil
import kr.co.hongstudio.sitezip.util.PermissionUtil
import kr.co.hongstudio.sitezip.util.ResourceProvider
import kr.co.hongstudio.sitezip.util.extension.observeBaseViewModelEvent
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class PlaceZipFragment : BaseFragment(), OnMapReadyCallback {

    companion object {
        const val TAG: String = "PlaceListFragment"

        fun newInstance(): PlaceZipFragment = PlaceZipFragment()
    }

    private val binding: FragmentPlaceZipBinding by lazy {
        FragmentPlaceZipBinding.bind(requireView())
    }

    private val viewModel: PlaceZipViewModel by sharedViewModel()

    private val permissionUtil: PermissionUtil by inject()

    private val resourceProvider: ResourceProvider by inject()

    private lateinit var keyboardUtil: KeyboardUtil

    private lateinit var naverMap: NaverMap

    private val mapMarkers: MutableList<Marker> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_place_zip, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initBinding()
        initViewModel()
        initPlacesRecyclerView()
        checkPermission()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.mapView.getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()
        if (!viewModel.locationUtil.checkLocationServicesStatus()) {
            showLocationServiceSetting()
        }
    }

    private fun initViewModel() {
        Log.d(TAG, "initViewModel: $viewModel")

        // 키보드 리스너 등록.
        context?.let { context ->
            activity?.window?.let { window ->
                keyboardUtil = KeyboardUtil(
                    applicationContext = context,
                    window = window,
                    onHideKeyboard = {
                        viewModel.checkKeyboardEnable(false)
                    },
                    onShowKeyboard = {
                        viewModel.checkKeyboardEnable(true)
                    }
                )
            }
        }

        binding.etSearchText.setOnEditorActionListener { _, _, _ ->
            viewModel.getPlaces(
                viewModel.searchText.value ?: return@setOnEditorActionListener false
            )
            true
        }
        viewModel.placeZip.observe(viewLifecycleOwner, Observer { placeZip ->
            if (placeZip.state == Model.TRUE) {
                createMapMarker(placeZip)
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
            Log.d(TAG, "viewModel.searchText.observe.")
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
        viewModel.selectMapMarker.observe(viewLifecycleOwner, EventObserver { placeName ->
            val selectMarker: Marker? = mapMarkers.findLast {
                it.captionText == placeName
            }
            selectMarker?.let {
                val cameraUpdate: CameraUpdate = CameraUpdate.scrollTo(
                    selectMarker.position
                ).animate(CameraAnimation.Easing)
                naverMap.moveCamera(cameraUpdate)
            }
        })
        observeBaseViewModelEvent(viewModel)
    }

    private fun checkPermission() {
        // 권한 요청
        permissionUtil.checkPermission(this,
            onGranted = {
                viewModel.setPermissionGranted(true)
            },
            onDenied = {
                // 게속요청
                checkPermission()
                viewModel.setPermissionGranted(false)
            }
        )
    }

    private fun createMapMarker(placeZip: PlaceZip) {
        if (mapMarkers.size > 0) {
            mapMarkers.forEach {
                it.map = null
            }
            mapMarkers.clear()
        }
        for (i: Int in placeZip.places.indices) {
            val tag = placeZip.id?.toInt()
            val name = placeZip.places[i].place_name
            val url = placeZip.places[i].place_url
            val latitude = placeZip.places[i].y?.toDouble()
            val longitude = placeZip.places[i].x?.toDouble()

            if (tag != null && latitude != null && longitude != null) {
                val marker: Marker = Marker().apply {
                    this.tag = tag
                    this.position = LatLng(latitude, longitude)
                    this.captionText = name ?: ""
                    this.icon = OverlayImage.fromResource(R.drawable.ic_baseline_place_24)
                    this.captionColor = resourceProvider.getColor(R.color.colorPrimary)
                    this.width = Marker.SIZE_AUTO
                    this.height = Marker.SIZE_AUTO
                    this.isHideCollidedSymbols = true
                    this.map = naverMap
                    name?.let {
                        this.setOnClickListener {
                            viewModel.findPlace(name)?.let {
                                viewModel.intentPlacePage(url)
                            }
                            true
                        }
                    }

                }
                mapMarkers.add(marker)
            }
            val bounds: LatLngBounds = LatLngBounds.Builder().include(
                mapMarkers.map { it.position }
            ).build()
            naverMap.moveCamera(CameraUpdate.fitBounds(bounds).animate(CameraAnimation.Easing))
        }
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

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
    }


}