package kr.co.hongstudio.sitezip.repositories.repository

import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.Disposable
import kr.co.hongstudio.sitezip.data.local.entity.Site

interface SiteRepository {

    fun getAllSites(): Flowable<List<Site>>

    fun insert(site: Site): Disposable

    fun delete(primaryKey: String): Disposable

    fun update(site: Site): Disposable

}