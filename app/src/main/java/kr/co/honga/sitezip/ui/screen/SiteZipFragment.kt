package kr.co.honga.sitezip.ui.screen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.honga.sitezip.R
import kr.co.honga.sitezip.base.fragment.BaseFragment
import kr.co.honga.sitezip.base.livedata.EventObserver
import kr.co.honga.sitezip.data.local.entity.SiteZip
import kr.co.honga.sitezip.databinding.FragmentSiteZipBinding
import kr.co.honga.sitezip.ui.screen.SiteZipViewModel.Serializable.SITE_ZIP
import kr.co.honga.sitezip.util.extension.observeBaseViewModelEvent
import org.koin.androidx.viewmodel.ext.android.getStateViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SiteZipFragment : BaseFragment() {

    companion object {
        const val TAG: String = "SiteTypeFragment"

        fun newInstance(siteZip: SiteZip): SiteZipFragment =
            SiteZipFragment().apply {
                arguments = bundleOf()
                arguments?.putParcelable(SITE_ZIP, siteZip)
            }
    }

    private val binding: FragmentSiteZipBinding by lazy {
        FragmentSiteZipBinding.bind(requireView())
    }

    val viewModel: SiteZipViewModel by lazy {
        getStateViewModel<SiteZipViewModel>(bundle = arguments)
    }

    private val mainViewModel: MainViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_site_zip, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initBinding()
        initViewModel()
        initRecordHistoriesRecyclerView()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
    }

    private fun initViewModel() {
        viewModel.intentUrlEvent.observe(viewLifecycleOwner, EventObserver { url ->
            val actionIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(actionIntent)
        })
        viewModel.siteZip.observe(viewLifecycleOwner, Observer {
            viewModel.getDisplaySiteType()
        })
        mainViewModel.isFavoriteMode.observe(viewLifecycleOwner, Observer {
            viewModel.setFavoriteMode(it)
            viewModel.getDisplaySiteType()
        })
        mainViewModel.searchText.observe(viewLifecycleOwner, Observer {
            viewModel.setSearchText(it)
            viewModel.getDisplaySiteType()
        })
        observeBaseViewModelEvent(viewModel)
    }

    /**
     * 녹취 내역 리사이클 뷰 초기화.
     */
    private fun initRecordHistoriesRecyclerView() {
        binding.rvSites.setHasFixedSize(true)
        binding.rvSites.itemAnimator = null
        binding.rvSites.layoutManager = LinearLayoutManager(activity)
        binding.rvSites.adapter = SitesAdapter(this, viewModel)
    }
}