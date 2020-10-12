package kr.co.hongstudio.sitezip.ui.screen.site

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.co.hongstudio.sitezip.R
import kr.co.hongstudio.sitezip.base.fragment.BaseFragment
import kr.co.hongstudio.sitezip.base.livedata.EventObserver
import kr.co.hongstudio.sitezip.data.local.entity.SiteZip
import kr.co.hongstudio.sitezip.databinding.FragmentSiteZipBinding
import kr.co.hongstudio.sitezip.ui.screen.MainViewModel
import kr.co.hongstudio.sitezip.ui.screen.site.SiteZipViewModel.Serializable.SITE_ZIP
import kr.co.hongstudio.sitezip.util.ResourceProvider
import kr.co.hongstudio.sitezip.util.extension.dismissDialog
import kr.co.hongstudio.sitezip.util.extension.dismissProgressDialog
import kr.co.hongstudio.sitezip.util.extension.observeBaseViewModelEvent
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getStateViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


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
    ): View = inflater.inflate(R.layout.fragment_site_zip, container, true)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initBinding()
        initViewModel()
        initRecordHistoriesRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.setSearchText("")
        mainViewModel.setSearchVisibility(false)
        mainViewModel.setSearchButtonVisible(true)
        mainViewModel.setFavoriteButtonVisible(true)
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.rvSites.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

    private fun initViewModel() {
        Log.d(TAG, "initViewModel. fragment.tag : ${this.tag}")
        // 사이트 정보 가져오기.
        viewModel.getSite()

        // 라이브데이터 옵저버 세팅.
        viewModel.intentUrlEvent.observe(viewLifecycleOwner, EventObserver { url ->
            try {
                val actionIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(actionIntent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        })
        viewModel.searchSiteZip.observe(viewLifecycleOwner, Observer {
            viewModel.displayTabViews()
        })
        viewModel.shareLink.observe(viewLifecycleOwner, EventObserver {
            val intent = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = SHARD_LINK_INTENT_TYPE
                putExtra(Intent.EXTRA_TEXT, it)
            }, resourceProvider.getString(R.string.site_link_share))
            startActivity(intent)
        })
        viewModel.scrollToPositionTop.observe(viewLifecycleOwner, EventObserver {
            (binding.rvSites.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, 0)
        })
        viewModel.checkVisibleProgress.observe(viewLifecycleOwner, Observer {
            mainViewModel.setCheckVisibleProgress(it)
        })
        mainViewModel.isFavoriteMode.observe(viewLifecycleOwner, Observer {
            viewModel.setFavoriteMode(it)
            viewModel.displayTabViews()
        })
        mainViewModel.searchText.observe(viewLifecycleOwner, Observer {
            viewModel.setSearchText(it)
            viewModel.displayTabViews()
        })
        observeBaseViewModelEvent(viewModel)
    }

    /**
     * 사이트 리사이클 뷰 초기화.
     */
    private fun initRecordHistoriesRecyclerView() {
        binding.rvSites.setHasFixedSize(true)
        binding.rvSites.itemAnimator = null
        binding.rvSites.layoutManager = LinearLayoutManager(activity)
        binding.rvSites.adapter =
            SitesAdapter(this, viewModel)
    }
}