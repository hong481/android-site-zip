package kr.co.hongstudio.sitezip.domain

import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.Moshi
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.hongstudio.sitezip.data.local.entity.Place
import kr.co.hongstudio.sitezip.data.remote.SmartPlaceApi
import kr.co.hongstudio.sitezip.util.extension.fromJson

class GetPlacesUseCase(

    private val moshi: Moshi,
    private val smartPlaceApi: SmartPlaceApi

) : UseCase<GetPlacesUseCase.Request, GetPlacesUseCase.Response>() {

    companion object {
        const val TAG: String = "GetPlacesUseCase"

        const val START = "1"
        const val DISPLAY = "1000"
        const val SORTING_ORDER = "reviewCount"
    }

    override fun request(
        request: Request,
        onResponse: (Response) -> Unit
    ): Disposable = smartPlaceApi.getSmartPlaces(
        start = START,
        display =DISPLAY,
        query = request.query,
        sortingOrder = SORTING_ORDER
    ).onBackpressureBuffer()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(
            onNext = { json ->
                Log.d(TAG, json.toString())
                onResponse(moshi.fromJson<Response>(json) ?: return@subscribeBy)
            },
            onError = {
                Log.d(TAG, it.toString())
            }
        )

    data class Request(
        var query: String
    )

    @JsonClass(generateAdapter = true)
    data class Response(
        override var code: Int? = null,

        override var message: String? = null,

        @Json(name = "total")
        var total: Int = 0,

        @Json(name = "items")
        var items: MutableList<Place>

    ) : UseCaseResponse(code, message)
}