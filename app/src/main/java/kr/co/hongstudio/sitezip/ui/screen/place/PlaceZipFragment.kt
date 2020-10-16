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
import kr.co.hongstudio.sitezip.R
import kr.co.hongstudio.sitezip.base.fragment.BaseFragment
import kr.co.hongstudio.sitezip.base.livedata.EventObserver
import kr.co.hongstudio.sitezip.base.model.Model
import kr.co.hongstudio.sitezip.data.local.entity.PlaceZip
import kr.co.hongstudio.sitezip.databinding.FragmentPlaceZipBinding
import kr.co.hongstudio.sitezip.ui.screen.OuterActivities
import kr.co.hongstudio.sitezip.util.*
import kr.co.hongstudio.sitezip.util.extension.observeBaseViewModelEvent
import kr.co.hongstudio.sitezip.util.extension.timer
import kr.co.hongstudio.sitezip.util.extension.wait
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class PlaceZipFragment : BaseFragment(), MapView.MapViewEventListener,
    MapView.POIItemEventListener {

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

    private val displayUtil: DisplayUtil by inject()

    private lateinit var keyboardUtil: KeyboardUtil

    private var mapView: MapView? = null

    private val mapPoiItems: MutableList<MapPOIItem> = mutableListOf()

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
    }

    override fun onRestart() {
        super.onRestart()
        initKakaoMapView()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart.")
        initKakaoMapView()
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
                        binding.rvPlaces.visibility = View.VISIBLE
                    },
                    onShowKeyboard = {
                        binding.rvPlaces.visibility = View.GONE
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
                createKakaoMapMarker(placeZip)
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
        viewModel.selectMapMarker.observe(viewLifecycleOwner, EventObserver { placeName ->
            val selectMarker: MapPOIItem? = mapPoiItems.findLast {
                it.itemName == placeName
            }
            selectMarker?.let {
                mapView?.selectPOIItem(selectMarker, true)
                mapView?.setMapCenterPoint(selectMarker.mapPoint, true)
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

    /**
     * 카카오맵 초기화.
     */
    private fun initKakaoMapView() {
        mapView?.let { binding.mapView.removeView(it) }
        mapView = null
        mapView = MapView(context).apply {
            setMapViewEventListener(this@PlaceZipFragment)
            setPOIItemEventListener(this@PlaceZipFragment)
        }
        mapView?.let { mapView ->
            binding.mapView.addView(mapView)
            viewModel.placeZip.value?.let { placeZip ->
                createKakaoMapMarker(placeZip)
            }
        }
    }

    private fun createKakaoMapMarker(placeZip: PlaceZip) {
        mapView?.removeAllPOIItems()
        mapPoiItems.clear()
        val mapPoiItemPoints: MutableList<MapPoint> = mutableListOf()
        for (i: Int in placeZip.places.indices) {
            val tag = placeZip.id?.toInt()
            val latitude = placeZip.places[i].y?.toDouble()
            val longitude = placeZip.places[i].x?.toDouble()

            if (tag != null && latitude != null && longitude != null) {
                val marker: MapPOIItem = MapPOIItem().apply {
                    this.tag = tag
                    this.itemName = placeZip.places[i].place_name
                    this.mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude)
                    this.markerType = MapPOIItem.MarkerType.BluePin
                    this.selectedMarkerType = MapPOIItem.MarkerType.RedPin
                }
                mapPoiItemPoints.add(marker.mapPoint)
                mapPoiItems.add(marker)
            }
        }
        if (mapPoiItems.size > 0) {
            timer(2000) {
                mapView?.addPOIItems(mapPoiItems.toTypedArray())
                mapView?.fitMapViewAreaToShowMapPoints(mapPoiItemPoints.toTypedArray())
            }
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

    override fun onMapViewInitialized(mapView: MapView?) {
        // empty
    }

    override fun onMapViewCenterPointMoved(mapView: MapView?, mapPoint: MapPoint?) {
        // empty
    }

    override fun onMapViewZoomLevelChanged(mapView: MapView?, p1: Int) {
        // empty
    }

    override fun onMapViewSingleTapped(mapView: MapView?, mapPoint: MapPoint?) {
        // empty
    }

    override fun onMapViewDoubleTapped(mapView: MapView?, mapPoint: MapPoint?) {
        // empty
    }

    override fun onMapViewLongPressed(mapView: MapView?, mapPoint: MapPoint?) {
        // empty
    }

    override fun onMapViewDragStarted(mapView: MapView?, mapPoint: MapPoint?) {
        // empty
    }

    override fun onMapViewDragEnded(mapView: MapView?, mapPoint: MapPoint?) {
        // empty
    }

    override fun onMapViewMoveFinished(mapView: MapView?, mapPoint: MapPoint?) {
        // empty
    }

    override fun onPOIItemSelected(mapView: MapView?, mapPOIItem: MapPOIItem?) {
        // empty
    }

    override fun onCalloutBalloonOfPOIItemTouched(mapView: MapView?, mapPOIItem: MapPOIItem?) {
        // empty
    }

    override fun onCalloutBalloonOfPOIItemTouched(
        mapView: MapView?,
        mapPOIItem: MapPOIItem?,
        calloutBalloonButtonType: MapPOIItem.CalloutBalloonButtonType?
    ) {
        viewModel.findPlace(mapPOIItem?.itemName ?: return)?.let {
            viewModel.intentPlacePage(it.place_url)
        }
    }

    override fun onDraggablePOIItemMoved(
        mapView: MapView?,
        mapPOIItem: MapPOIItem?,
        mapPoint: MapPoint?
    ) {
        // empty
    }
}