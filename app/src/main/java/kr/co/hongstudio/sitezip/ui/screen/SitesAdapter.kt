package kr.co.hongstudio.sitezip.ui.screen

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import kr.co.hongstudio.sitezip.base.adapter.BaseRecyclerViewAdapter
import kr.co.hongstudio.sitezip.base.adapter.BaseRecyclerViewHolder
import kr.co.hongstudio.sitezip.data.local.entity.Site

class SitesAdapter (

    private val lifecycleOwner: LifecycleOwner,
    private val viewModel: SiteViewHolder.ViewModel

) : BaseRecyclerViewAdapter<Site> () {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseRecyclerViewHolder<Site> = SiteViewHolder(parent, viewModel, lifecycleOwner)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val adapterPosition: Int = holder.adapterPosition.takeIf {
            it != RecyclerView.NO_POSITION
        } ?: return

        val site: Site = items[adapterPosition]

        when (holder) {
            is SiteViewHolder -> holder.onBind(site)
        }
    }

}