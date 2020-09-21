package kr.co.hongstudio.sitezip.ui.screen.place

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import kr.co.hongstudio.sitezip.R
import kr.co.hongstudio.sitezip.base.adapter.BaseRecyclerViewHolder
import kr.co.hongstudio.sitezip.data.local.entity.Place
import kr.co.hongstudio.sitezip.databinding.ItemPlaceBinding
import kr.co.hongstudio.sitezip.util.extension.map

class PlacesHolder(

    viewGroup: ViewGroup,
    viewModel: ViewModel,
    lifecycleOwner: LifecycleOwner

) : BaseRecyclerViewHolder<Place>(viewGroup, R.layout.item_place) {

    private val binding: ItemPlaceBinding by lazy {
        ItemPlaceBinding.bind(itemView)
    }

    /**
     * 전화 번호.
     */
    val phoneNumber: LiveData<String> = item.map {
        if (it?.phone != null || !it?.phone.isNullOrEmpty()) {
            it?.phone
        } else {
            if (it?.virtualPhone != null || !it?.phone.isNullOrEmpty()) {
                it?.virtualPhone
            } else {
                null
            }
        }
    }

    /**
     * 영업 시간.
     */
    val bizHourInfo: LiveData<String> = item.map {
        if (it?.bizHourInfo != null || !it?.bizHourInfo.isNullOrEmpty()) {
            if (it?.bizHourInfo?.length ?: 0 > 5) {
                it?.bizHourInfo
            } else {
                null
            }
        } else {
            null
        }
    }

    init {
        binding.viewHolder = this
        binding.viewModel = viewModel
        binding.lifecycleOwner = lifecycleOwner
    }

    interface ViewModel {
        fun intentPlacePage(placeId: Long?)
    }

}