package kr.co.hongstudio.sitezip.domain

import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.hongstudio.sitezip.data.local.entity.Place
import kr.co.hongstudio.sitezip.data.remote.PlaceApi
import kr.co.hongstudio.sitezip.util.extension.fromJson

class GetPlacesUseCase(

    private val moshi: Moshi,
    private val smartPlaceApi: PlaceApi

) : UseCase<GetPlacesUseCase.Request, GetPlacesUseCase.Response>() {

    companion object {
        const val TAG: String = "GetPlacesUseCase"

        const val PAGE = "45"
        const val SIZE = "15"
    }

    override fun request(
        request: Request,
        onResponse: (Response) -> Unit
    ): Disposable = smartPlaceApi.getSmartPlaces(
        url = request.placeApi,
        header = request.apiKey,
        query = request.query,
        page = PAGE,
        size = SIZE,
        x = request.x,
        y = request.y
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
        var placeApi: String,
        var apiKey: String,
        var query: String,
        var x: String,
        var y: String
    )

    @JsonClass(generateAdapter = true)
    data class Response(
        override var code: Int? = null,

        override var message: String? = null,

        @Json(name = "documents")
        var items: MutableList<Place>

    ) : UseCaseResponse(code, message)
}