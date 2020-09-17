package kr.co.hongstudio.sitezip.domain

import android.util.Log
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.hongstudio.sitezip.data.remote.SmartPlaceApi
import kr.co.hongstudio.sitezip.util.extension.fromJson

class GetPlacesUseCase(

    private val moshi: Moshi,
    private val smartPlaceApi: SmartPlaceApi

) : UseCase<GetPlacesUseCase.Request, GetPlacesUseCase.Response>() {

    companion object {
        const val TAG: String = "GetPlacesUseCase"
    }

    override fun request(
        request: Request,
        onResponse: (Response) -> Unit
    ): Disposable = smartPlaceApi.getSmartPlaces(
        start = request.start,
        display = request.display,
        query = request.query,
        sortingOrder = request.sortingOrder
    ).onBackpressureBuffer()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(
            onNext = {
                Log.d(TAG, it.toString())
            },
            onError = {
                Log.d(TAG, it.toString())
            }
        )

    data class Request(
        var start: String,
        var display: String,
        var query: String,
        var sortingOrder: String
    )

    @JsonClass(generateAdapter = true)
    data class Response(
        override var code: Int? = null,

        override var message: String? = null

//        val places: MutableList<Place> = mutableListOf()

    ) : UseCaseResponse(code, message)
}