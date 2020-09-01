package kr.co.hongstudio.sitezip.ui.screen

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.hongstudio.sitezip.R
import kr.co.hongstudio.sitezip.base.fragment.BaseFragment
import kr.co.hongstudio.sitezip.base.livedata.EventObserver
import kr.co.hongstudio.sitezip.data.local.entity.SiteZip
import kr.co.hongstudio.sitezip.databinding.FragmentSiteZipBinding
import kr.co.hongstudio.sitezip.ui.screen.SiteZipViewModel.Serializable.SITE_ZIP
import kr.co.hongstudio.sitezip.util.ResourceProvider
import kr.co.hongstudio.sitezip.util.extension.observeBaseViewModelEvent
import org.koin.androidx.viewmodel.ext.android.getStateViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.android.ext.android.inject


class SiteZipFragment : BaseFragment() {

    companion object {
        const val TAG: String = "SiteTypeFragment"

        const val SHARD_LINK_INTENT_TYPE: String = "text/plain"

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

    private val resourceProvider: ResourceProvider by inject()

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
            try {
                val actionIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(actionIntent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        })
        viewModel.siteZip.observe(viewLifecycleOwner, Observer {
            viewModel.getDisplaySiteType()
        })
        viewModel.shareLink.observe(viewLifecycleOwner, EventObserver {
            val intent = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = SHARD_LINK_INTENT_TYPE
                putExtra(Intent.EXTRA_TEXT, it)
            }, resourceProvider.getString(R.string.site_link_share))
            startActivity(intent)
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