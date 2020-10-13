package kr.co.hongstudio.sitezip.ui.screen.site

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import kr.co.hongstudio.sitezip.data.local.entity.SiteZip

class SiteZipFragmentAdapter(

    val fragmentActivity: FragmentActivity

) : FragmentStateAdapter(fragmentActivity) {

    var adapterItemsSize: Int = 0

    var adapterItems: MutableList<SiteZip> = mutableListOf()

    override fun getItemCount(): Int = adapterItems.size

    override fun createFragment(position: Int): Fragment =
        SiteZipFragment.newInstance(siteZip = adapterItems[position])

    fun setItems(items: MutableList<SiteZip>) {
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

        val fragment: SiteZipFragment =
            (this.fragmentActivity.supportFragmentManager.findFragmentByTag(
                "f" + holder.itemId
            ) as? SiteZipFragment) ?: return
        fragment.viewModel.onBind(adapterItems[adapterPosition])
    }
}