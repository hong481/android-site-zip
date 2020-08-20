package kr.co.honga.sitezip.ui.screen

import android.content.ClipData
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kr.co.honga.sitezip.R
import kr.co.honga.sitezip.base.adapter.BaseRecyclerViewHolder
import kr.co.honga.sitezip.data.local.entity.Site
import kr.co.honga.sitezip.databinding.ItemSiteBinding
import kr.co.honga.sitezip.util.extension.map

class SiteViewHolder(

    viewGroup: ViewGroup,
    viewModel: ViewModel,
    lifecycleOwner: LifecycleOwner

) : BaseRecyclerViewHolder<Site>(viewGroup, R.layout.item_site) {

    private val binding: ItemSiteBinding by lazy {
        ItemSiteBinding.bind(itemView)
    }

    val imageReference: LiveData<StorageReference> = item.map {
        FirebaseStorage.getInstance().getReference(item.value?.siteIconUrl ?: "")
    }

    init {
        binding.viewHolder = this
        binding.viewModel = viewModel
        binding.lifecycleOwner = lifecycleOwner
    }

    interface ViewModel {
        fun intentUrl(url: String)

        fun copyLink(label: String, text: String)
    }

}