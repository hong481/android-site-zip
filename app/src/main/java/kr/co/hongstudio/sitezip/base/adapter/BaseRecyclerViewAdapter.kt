package kr.co.hongstudio.sitezip.base.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kr.co.hongstudio.sitezip.base.model.Model

abstract class BaseRecyclerViewAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    open var items: MutableList<T> = mutableListOf()
        set(value) {
            val diffCallback: DiffCallback<T> = DiffCallback(field, value)
            val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(diffCallback)

            field.clear()
            field.addAll(value)

            diffResult.dispatchUpdatesTo(this)
        }

    init {
        setHasStableIds(false)
    }

    override fun getItemCount(): Int = this.items.size

    final override fun setHasStableIds(hasStableIds: Boolean) =
        super.setHasStableIds(hasStableIds)

    override fun getItemId(position: Int): Long = this.items[position].let { item ->
        return when (hasStableIds()) {
            true -> return when (item) {
                is Model -> item.id ?: Model.NONE_ID
                else -> position.toLong()
            }
            else -> super.getItemId(position)
        }
    }

}