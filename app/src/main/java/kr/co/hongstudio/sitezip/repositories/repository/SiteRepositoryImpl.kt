package kr.co.hongstudio.sitezip.repositories.repository

import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.hongstudio.sitezip.data.local.entity.Site
import kr.co.hongstudio.sitezip.repositories.daos.SiteDao
import kr.co.hongstudio.sitezip.util.extension.toV3

class SiteRepositoryImpl(

    private val siteDao: SiteDao

) : SiteRepository {

    companion object {
        const val TAG: String = "SiteRepositoryImpl"
    }

    override fun checkFavoriteSite(primaryKey: String): Flowable<Boolean> =
        siteDao.checkFavoriteSite(primaryKey).toV3().distinctUntilChanged()


    override fun insert(
        site: Site
    ): Disposable = siteDao.insert(site)
        .toV3()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(
            onSuccess = { id ->
                site.id = id
                Log.d(TAG, "insert. success")
            },
            onError = {
                Log.d(TAG, it.toString())
            }
        )

    override fun delete(
        primaryKey: String
    ): Disposable = siteDao.delete(primaryKey)
        .toV3<Unit>()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(
            onComplete = {
                Log.d(TAG, "delete. success")
            },
            onError = {
                Log.d(TAG, it.toString())
            }
        )

    override fun update(
        site: Site
    ): Disposable = siteDao.update(site)
        .toV3<Unit>()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(
            onComplete = {
                Log.d(TAG, "update. success")
            },
            onError = {
                Log.d(TAG, it.toString())
            }
        )
}