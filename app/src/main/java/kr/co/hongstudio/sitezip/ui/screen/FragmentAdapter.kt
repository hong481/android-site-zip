package kr.co.hongstudio.sitezip.ui.screen

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import kr.co.hongstudio.sitezip.base.model.Model
import kr.co.hongstudio.sitezip.data.local.entity.Place
import kr.co.hongstudio.sitezip.data.local.entity.SiteZip
import kr.co.hongstudio.sitezip.ui.screen.place.PlaceListFragment
import kr.co.hongstudio.sitezip.ui.screen.site.SiteZipFragment

class FragmentAdapter(

    val fragmentActivity: FragmentActivity

) : FragmentStateAdapter(fragmentActivity) {

    var adapterItemsSize: Int = 0

    var adapterItems: MutableList<Model> = mutableListOf()

    override fun getItemCount(): Int = adapterItems.size

    override fun createFragment(position: Int): Fragment = when {
        adapterItems[position] is Place -> {
            PlaceListFragment.newInstance(place = adapterItems[position] as Place)
        }
        else -> {
            SiteZipFragment.newInstance(siteZip = adapterItems[position] as SiteZip)
        }
    }

    fun setItems(items: MutableList<Model>) {
        this.adapterItems = items
        notifyDataSetChanged()
    }

    fun setSize(size: Int) {
        adapterItemsSize = size
    }

    override fun onBindViewHolder(
        holder: FragmentViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        val adapterPosition: Int = holder.adapterPosition.takeIf {
            it != RecyclerView.NO_POSITION
        } ?: return

        when {
            adapterItems[position] is Place -> {
                val fragment: PlaceListFragment =
                    (this.fragmentActivity.supportFragmentManager.findFragmentByTag(
                        "f" + holder.itemId
                    ) as? PlaceListFragment) ?: return
            }
            adapterItems[position] is SiteZip -> {
                val fragment: SiteZipFragment =
                    (this.fragmentActivity.supportFragmentManager.findFragmentByTag(
                        "f" + holder.itemId
                    ) as? SiteZipFragment) ?: return
                fragment.viewModel.onBind(adapterItems[adapterPosition] as SiteZip)
            }
        }
    }
}