package kr.co.honga.sitezip.ui.screen

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import kr.co.honga.sitezip.base.adapter.DiffCallback
import kr.co.honga.sitezip.base.model.Model
import kr.co.honga.sitezip.data.local.entity.SiteType

class SiteTypesAdapter(

    fragmentActivity: FragmentActivity

) : FragmentStateAdapter(fragmentActivity) {

    var siteTypes: MutableList<SiteType> = mutableListOf()
        set(value) {
            val diffCallback: DiffCallback<SiteType> = DiffCallback(field, value)
            val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(diffCallback)

            field.clear()
            field.addAll(value)

            diffResult.dispatchUpdatesTo(this)
        }

    init {
        setHasStableIds(false)
    }

    override fun onBindViewHolder(
        holder: FragmentViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        
    }

    override fun getItemCount(): Int = siteTypes.size

    override fun createFragment(position: Int): Fragment =
        SiteTypeFragment.newInstance(siteType = siteTypes[position])

    fun setItems(siteTypes: MutableList<SiteType>) {
        this.siteTypes = siteTypes
        notifyDataSetChanged()
    }
}