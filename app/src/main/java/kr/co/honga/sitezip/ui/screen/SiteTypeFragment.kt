package kr.co.honga.sitezip.ui.screen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.honga.sitezip.R
import kr.co.honga.sitezip.base.fragment.BaseFragment
import kr.co.honga.sitezip.base.livedata.EventObserver
import kr.co.honga.sitezip.data.local.entity.SiteType
import kr.co.honga.sitezip.databinding.FragmentSitesBinding
import kr.co.honga.sitezip.ui.screen.SiteTypesViewModel.Serializable.SITE
import org.koin.androidx.viewmodel.ext.android.getStateViewModel

class SiteTypeFragment : BaseFragment() {

    companion object {
        const val TAG: String = "SiteTypeFragment"

        fun newInstance(siteType: SiteType): SiteTypeFragment = SiteTypeFragment().apply {
            arguments = bundleOf()
            arguments?.putParcelable(SITE, siteType)
        }
    }

    private val binding: FragmentSitesBinding by lazy {
        FragmentSitesBinding.bind(requireView())
    }

    val viewModel: SiteTypesViewModel by lazy {
        getStateViewModel<SiteTypesViewModel>(bundle = arguments)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_sites, container, false)

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
    }

    /**
     * 녹취 내역 리사이클 뷰 초기화.
     */
    private fun initRecordHistoriesRecyclerView() {
        binding.rvSites.setHasFixedSize(true)
        binding.rvSites.layoutManager = LinearLayoutManager(activity)
        binding.rvSites.adapter = SitesAdapter(this, viewModel)
    }
}