package kr.co.irlink.irsdk.network.http

import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.io.File

open class OkHttpUtil {

    val applicationJson: MediaType = MediaType.parse("application/json;")!!

    val multipartFormData: MediaType = MediaType.parse("multipart/form-data")!!

    open fun createRequestBody(value: String): RequestBody =
        RequestBody.create(applicationJson, value)

    open fun createRequestBody(value: ByteArray): RequestBody =
        RequestBody.create(applicationJson, value)

    open fun createRequestBody(value: Int = 0): RequestBody =
        RequestBody.create(applicationJson, value.toString())

    open fun createRequestBody(value: Boolean): RequestBody =
        RequestBody.create(applicationJson, value.toString())

    open fun createRequestBody(file: File): RequestBody =
        RequestBody.create(multipartFormData, file)

    open fun createMultiPart(key: String, uploadFileName: String, file: File): MultipartBody.Part =
        MultipartBody.Part.createFormData(
            key,
            uploadFileName,
            createRequestBody(file)
        )

    open fun createFormData(key: String, value: String): MultipartBody.Part =
        MultipartBody.Part.createFormData(key, value)

    open fun createFormData(key: String, file: File): MultipartBody.Part =
        MultipartBody.Part.createFormData(
            key,
            file.name,
            createRequestBody(file)
        )

    open fun createMultiPartKey(key: String, file: File): String =
        createMultiPartKey(key, file.name)

    open fun createMultiPartKey(key: String, fileName: String): String =
        "$key\"; filename=\"$fileName\""

    open fun createResponseBody(json: String): ResponseBody =
        ResponseBody.create(applicationJson, json)

}
