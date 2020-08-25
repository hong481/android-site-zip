package kr.co.honga.sitezip.repositories.daos

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import kr.co.honga.sitezip.data.local.entity.Site

@Dao
interface SiteDao {

    /**
     * 데이터를 반환.
     */
    @Query("SELECT * FROM Site")
    fun getAllSites(): Flowable<List<Site>>

    /**
     * 사이트 정보 삽입.
     */
    @Insert
    fun insert(site: Site): Single<Long>

    /**
     * 사이트 정보 삭제.
     */
    @Query("DELETE FROM Site WHERE site_primary_key = :primaryKey")
    fun delete(primaryKey: String): Completable


    /**
     * 사이트 정보 업데이트.
     */
    @Update
    fun update(vararg site: Site): Completable
}