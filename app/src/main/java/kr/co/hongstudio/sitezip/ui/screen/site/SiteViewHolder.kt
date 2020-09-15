package kr.co.hongstudio.sitezip.ui.screen.site

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import kr.co.hongstudio.sitezip.R
import kr.co.hongstudio.sitezip.base.adapter.BaseRecyclerViewHolder
import kr.co.hongstudio.sitezip.data.local.entity.Site
import kr.co.hongstudio.sitezip.databinding.ItemSiteBinding
import kr.co.hongstudio.sitezip.util.extension.map

class SiteViewHolder(

    viewGroup: ViewGroup,
    viewModel: ViewModel,
    lifecycleOwner: LifecycleOwner

) : BaseRecyclerViewHolder<Site>(viewGroup, R.layout.item_site) {

    private val binding: ItemSiteBinding by lazy {
        ItemSiteBinding.bind(itemView)
    }

    val imageUrl: LiveData<String> = item.map {
        item.value?.iconUrl ?: ""
    }

    init {
        binding.viewHolder = this
        binding.viewModel = viewModel
        binding.lifecycleOwner = lifecycleOwner
    }

    interface ViewModel {
        fun intentUrl(url: String)

        fun copyLink(label: String, text: String)

        fun shareLink(text: String)

        fun chooseFavorite(site: Site)

        fun releaseFavorite(site: Site)
    }

}