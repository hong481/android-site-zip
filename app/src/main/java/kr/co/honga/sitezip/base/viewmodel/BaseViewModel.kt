package kr.co.honga.sitezip.base.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kr.co.honga.sitezip.base.livedata.EmptyEvent
import kr.co.honga.sitezip.base.livedata.Event
import kr.co.honga.sitezip.util.extension.notify
import kr.co.honga.sitezip.util.extension.postNotify

abstract class BaseViewModel : ViewModel() {

    private val _showToast: MutableLiveData<Event<String>> = MutableLiveData()
    val showToast: LiveData<Event<String>> = _showToast

    private val _showProgress: MutableLiveData<EmptyEvent> = MutableLiveData()
    val showProgress: LiveData<EmptyEvent> = _showProgress

    private val _dismissProgress: MutableLiveData<EmptyEvent> = MutableLiveData()
    val dismissProgress: LiveData<EmptyEvent> = _dismissProgress

    protected val compositeDisposable: CompositeDisposable = CompositeDisposable()

    @JvmOverloads
    open fun showToast(message: String?, isPost: Boolean = false) = when (isPost) {
        true -> _showToast.postNotify = message
        else -> _showToast.notify = message
    }

    @JvmOverloads
    open fun showProgress(isPost: Boolean = false) = when (isPost) {
        true -> _showProgress.postNotify()
        else -> _showProgress.notify()
    }

    @JvmOverloads
    open fun dismissProgress(isPost: Boolean = false) = when (isPost) {
        true -> _dismissProgress.postNotify()
        else -> _dismissProgress.notify()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

}
