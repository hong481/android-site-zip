package kr.co.honga.sitezip.base.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kr.co.honga.sitezip.util.extension.setValueIfNew
import org.koin.core.KoinComponent

abstract class BaseRecyclerViewHolder<T>(

    itemView: View

) : RecyclerView.ViewHolder(itemView), ViewHolder, KoinComponent {

    private val _item: MutableLiveData<T> = MutableLiveData()
    val item: LiveData<T> = _item

    override val compositeDisposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    constructor(parent: ViewGroup, layoutRes: Int) : this(
        LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
    )

    open fun onBind(item: T?) {
        _item setValueIfNew item
    }

}