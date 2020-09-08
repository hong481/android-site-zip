package kr.co.hongstudio.sitezip.repositories.daos

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import kr.co.hongstudio.sitezip.data.local.entity.Site

@Dao
interface SiteDao {

    /**
     * 즐겨찾기 저장 여부 조회.
     */
    @Query("SELECT EXISTS (SELECT * FROM Site WHERE site_primary_key = :primaryKey)")
    fun checkFavoriteSite(primaryKey: String): Flowable<Boolean>

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