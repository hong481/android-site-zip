package kr.co.honga.sitezip.ui.screen

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import kr.co.honga.sitezip.data.local.entity.SiteType

class SiteTypesAdapter(

    val fragmentActivity: FragmentActivity

) : FragmentStateAdapter(fragmentActivity) {

    var siteTypes: MutableList<SiteType> = mutableListOf()

    override fun getItemCount(): Int = siteTypes.size

    override fun createFragment(position: Int): Fragment =
        SiteTypeFragment.newInstance(siteType = siteTypes[position])

    fun setItems(siteTypes: MutableList<SiteType>) {
        this.siteTypes = siteTypes
        notifyDataSetChanged()
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

        val fragment: SiteTypeFragment =
            (this.fragmentActivity.supportFragmentManager.findFragmentByTag("f" + holder.itemId) as? SiteTypeFragment) ?: return

        fragment.viewModel.onBind(siteTypes[adapterPosition])
    }
}