package kr.co.honga.sitezip.util.extension

import kr.co.honga.sitezip.base.livedata.EmptyEvent
import kr.co.honga.sitezip.base.livedata.Event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations

var <T> MutableLiveData<T>.postValue: T?
    set(value) {
        postValue(value)
    }
    get() = value

var <T> MutableLiveData<Event<T>>.notify: T?
    set(value) {
        setValue(Event(value))
    }
    get() = value?.data

var <T> MutableLiveData<Event<T>>.postNotify: T?
    set(value) {
        postValue(Event(value))
    }
    get() = value?.data

fun <X, Y> LiveData<X>.map(body: (X?) -> Y?): LiveData<Y> =
    Transformations.map(this, body)

fun <X, Y> LiveData<X>.switchMap(body: (X?) -> LiveData<Y>): LiveData<Y> =
    Transformations.switchMap(this, body)

infix fun <T> MutableLiveData<T>.setValueIfNew(newValue: T?) {
    if (this.value != newValue) value = newValue
}

infix fun <T> MutableLiveData<T>.postValueIfNew(newValue: T?) {
    if (this.value != newValue) postValue(newValue)
}

fun <T> MutableLiveData<T>.refresh(ifNewValue: Boolean = false, isPostUpdate: Boolean = false) = when (isPostUpdate) {
    true -> when (ifNewValue) {
        true -> postValueIfNew(value)
        else -> postValue(value)
    }
    else -> when (ifNewValue) {
        true -> setValueIfNew(value)
        else -> setValue(value)
    }
}

fun MutableLiveData<EmptyEvent>.notify() {
    value = EmptyEvent()
}

fun MutableLiveData<EmptyEvent>.postNotify() {
    postValue = EmptyEvent()
}