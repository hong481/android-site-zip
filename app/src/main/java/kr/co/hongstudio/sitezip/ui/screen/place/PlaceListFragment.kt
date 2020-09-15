package kr.co.hongstudio.sitezip.ui.screen.place

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import kr.co.hongstudio.sitezip.R
import kr.co.hongstudio.sitezip.base.fragment.BaseFragment
import kr.co.hongstudio.sitezip.data.local.entity.Place
import kr.co.hongstudio.sitezip.databinding.FragmentPlaceListBinding
import kr.co.hongstudio.sitezip.ui.screen.MainViewModel
import kr.co.hongstudio.sitezip.ui.screen.place.PlaceListViewModel.Serializable.PLACE
import kr.co.hongstudio.sitezip.util.ResourceProvider
import kr.co.hongstudio.sitezip.util.extension.observeBaseViewModelEvent
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getStateViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class PlaceListFragment : BaseFragment() {

    companion object {
        const val TAG: String = "PlacesFragment"

        fun newInstance(place: Place): PlaceListFragment =
            PlaceListFragment().apply {
                arguments = bundleOf()
                arguments?.putParcelable(PLACE, place)
            }
    }

    private val binding: FragmentPlaceListBinding by lazy {
        FragmentPlaceListBinding.bind(requireView())
    }

    val viewModel: PlaceListViewModel by lazy {
        getStateViewModel<PlaceListViewModel>(bundle = arguments)
    }

    private val mainViewModel: MainViewModel by sharedViewModel()

    private val resourceProvider: ResourceProvider by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_place_list, container, true)

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
        observeBaseViewModelEvent(viewModel)
    }
}