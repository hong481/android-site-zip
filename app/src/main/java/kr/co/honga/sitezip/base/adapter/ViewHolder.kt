package kr.co.honga.sitezip.base.adapter

import io.reactivex.rxjava3.disposables.CompositeDisposable


interface ViewHolder {

    val compositeDisposable: CompositeDisposable

}