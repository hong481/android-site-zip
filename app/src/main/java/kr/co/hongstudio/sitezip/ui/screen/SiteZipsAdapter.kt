package kr.co.hongstudio.sitezip.ui.screen

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import kr.co.hongstudio.sitezip.data.local.entity.SiteZip

class SiteZipsAdapter(

    val fragmentActivity: FragmentActivity

) : FragmentStateAdapter(fragmentActivity) {

    var siteZipsSize: Int = 0

    var siteZips: MutableList<SiteZip> = mutableListOf()

    override fun getItemCount(): Int = siteZips.size

    override fun createFragment(position: Int): Fragment =
        SiteZipFragment.newInstance(siteZip = siteZips[position])

    fun setItems(siteZips: MutableList<SiteZip>) {
        this.siteZips = siteZips
        notifyDataSetChanged()
    }

    fun setSize(size: Int) {
        siteZipsSize = size
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

        fragment.viewModel.onBind(siteZips[adapterPosition])
    }
}