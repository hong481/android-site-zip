package kr.co.hongstudio.sitezip.data.remote

import io.reactivex.rxjava3.core.Flowable
import retrofit2.http.*

interface PlaceApi {

    /**
     * 스마트플레이스 정보 요청.
     */
    @POST
    fun getSmartPlaces(
        @Url url: String,
        @Header("Authorization") header: String,
        @Query("query") query: String,
        @Query("page") page: String,
        @Query("size") size: String,
        @Query("x") x: String,
        @Query("y") y: String
    ): Flowable<String>

}