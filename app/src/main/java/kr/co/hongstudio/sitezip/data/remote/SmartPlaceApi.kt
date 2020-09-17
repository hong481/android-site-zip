package kr.co.hongstudio.sitezip.data.remote

import io.reactivex.rxjava3.core.Flowable
import retrofit2.http.GET
import retrofit2.http.Query

interface SmartPlaceApi {

    /**
     * 스마트플레이스 정보 요청.
     */
    @GET("/sogum/api/businesses")
    fun getSmartPlaces(
        @Query("start") start: String,
        @Query("display") display: String,
        @Query("query") query: String,
        @Query("sortingOrder") sortingOrder: String
    ): Flowable<String>

}