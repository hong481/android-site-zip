package kr.co.hongstudio.sitezip.ui.screen.place

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import kr.co.hongstudio.sitezip.base.adapter.BaseRecyclerViewAdapter
import kr.co.hongstudio.sitezip.base.adapter.BaseRecyclerViewHolder
import kr.co.hongstudio.sitezip.data.local.entity.Place

class PlacesAdapter (

    private val lifecycleOwner: LifecycleOwner,
    private val viewModel: PlacesHolder.ViewModel

) : BaseRecyclerViewAdapter<Place> () {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseRecyclerViewHolder<Place> =
        PlacesHolder(
            parent,
            viewModel,
            lifecycleOwner
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val adapterPosition: Int = holder.adapterPosition.takeIf {
            it != RecyclerView.NO_POSITION
        } ?: return

        val place: Place = items[adapterPosition]

        when (holder) {
            is PlacesHolder -> holder.onBind(place)
        }
    }

}