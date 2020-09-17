package kr.co.hongstudio.sitezip.util.http

import android.util.Log
import kr.co.hongstudio.sitezip.BuildConfig
import okhttp3.*
import okhttp3.internal.Util
import okio.Buffer
import okio.BufferedSource
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset

class OkHttpInterceptor : Interceptor {

    companion object {
        const val TAG: String = "OkHttpInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain?): Response {

        // original request
        val request: Request = chain?.request()
            ?: throw KotlinNullPointerException("request is null.")

        // new url
        val newUrl: HttpUrl = addQueryData(request.url())

        // new request and print
        val newRequest: Request = addHeaderData(request, newUrl).also {
            printRequest(it)
        }

        // new response and print
        val response: Response? = newRequest.let {
            chain.proceed(newRequest)

        }.also {
            printResponse(it)
        }

        // return response
        return response ?: throw NullPointerException("The response of retrofit2 is null.")
    }

    private fun addQueryData(httpUrl: HttpUrl): HttpUrl = httpUrl.newBuilder()
        .build()

    private fun addHeaderData(request: Request, url: HttpUrl): Request = request.newBuilder()
        .url(url)
        .build()

    private fun printRequest(newRequest: Request?) {
        if (!BuildConfig.DEBUG) {
            return
        }
        val url: HttpUrl? = newRequest?.url()
        val method: String? = newRequest?.method()
        val header: Headers? = newRequest?.headers()
        val body: RequestBody? = newRequest?.body()
        val contentSubType: String? = body?.contentType()?.subtype()

        // method + url
        url?.let {
            Log.d(TAG, "Request Url = [$method] $it")
        }

        // header
        val toJsonHeader: String? = header?.toJson()
        Log.d(TAG, "Request Header -> $toJsonHeader")

        if (method == "GET") {
            // query
            Log.d(TAG, "Request Queries -> ${url?.queriesToJson()}")

        } else {
            // body
            Log.d(TAG, "Request Header -> $contentSubType")
            val formatRequestBody: String? = when (contentSubType) {
                "json" -> body.toJson()
                "form-data" -> body.toFormData()
                "x-www-form-urlencoded" -> body.toUrlEncoded()
                else -> ""
            }
            Log.d(TAG, "Request Body -> $formatRequestBody")
        }
    }

    private fun HttpUrl.queriesToJson(): String = JSONObject().apply {
        for (key: String in queryParameterNames()) {
            put(key, queryParameter(key))
        }
    }.toString(4)

    private fun printResponse(response: Response?) {
        if (!BuildConfig.DEBUG) {
            return
        }
        // request path
        val requestPath: String? = response?.request()?.url()?.encodedPath()

        // response code + isSuccessful + message
        Log.d(
            TAG,
            "Response State = [${response?.code()}, ${response?.isSuccessful}] ${response?.message()}"
        )

        // response body
        Log.d(TAG, "Response($requestPath) -> ${response?.toJson()}")
    }

    private fun Headers.toJson(): String? = StringBuilder().also { jsonHeader ->
        jsonHeader.append("{")
        jsonHeader.append("\n")

        for (i: Int in 0 until size()) {
            val key: String = name(i)
            val value: String = value(i)

            jsonHeader.append("\t")
            jsonHeader.append("\"$key\"")
            jsonHeader.append(":")
            jsonHeader.append("\"$value\"")

            if (i != size() - 1) {
                jsonHeader.append(",")
            }
            jsonHeader.append("\n")
        }
        jsonHeader.append("}")

    }.toString()

    private fun RequestBody.toJson(): String? = try {
        val buffer: String = Buffer().let {
            writeTo(it)
            it.readUtf8()
        }
        try {
            JSONObject(buffer).toString(4)
        } catch (e: JSONException) {
            buffer
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }

    private fun Response.toJson(): String? {
        val source: BufferedSource? = body()?.source()
        source?.request(Long.MAX_VALUE)

        val buffer: Buffer? = source?.buffer?.clone()
        val charset: Charset = Util.bomAwareCharset(source, charset("UTF-8"))
        val responseBody: String = buffer?.readString(charset) ?: return null

        return try {
            JSONObject(responseBody).toString(4)

        } catch (e: Exception) {
            responseBody
        }
    }

    private fun RequestBody.toFormData(): String? = try {
        val toJsonObject: JSONObject = JSONObject()
        val multipartBody: MultipartBody = this as MultipartBody

        for (part: MultipartBody.Part in multipartBody.parts()) {
            when (part.body().contentType()?.type()) {
                "multipart" -> {
                    val key: String = part.getKey()
                    val value: String = part.getFileName()

                    val jsonArray: JSONArray? = toJsonObject.optJSONArray(key)

                    when (jsonArray) {
                        null -> toJsonObject.put(key, JSONArray().apply {
                            put(value)
                        })
                        else -> jsonArray.put(value)
                    }
                }
                else -> {
                    val key: String = part.getKey()
                    val strJson: String = part.body().toJson() ?: ""
                    try {
                        toJsonObject.put(key, JSONObject(strJson))

                    } catch (e: JSONException) {
                        toJsonObject.put(key, strJson)
                    }
                }
            }
        }
        toJsonObject.toString(4)

    } catch (e: JSONException) {
        e.printStackTrace()
        null

    } catch (e: IOException) {
        e.printStackTrace()
        null
    }

    private fun RequestBody.toUrlEncoded(): String? = try {
        val toJsonObject: JSONObject = JSONObject()
        val formBody = this as FormBody

        for (i: Int in 0 until formBody.size()) {
            val key = formBody.encodedName(i)
            val value = formBody.encodedValue(i)

            toJsonObject.put(key, value)
        }
        toJsonObject.toString(4)

    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    private fun MultipartBody.Part.getKey(): String = headers()
        ?.value(0)
        ?.split(";")
        ?.get(1)
        ?.split("=")
        ?.get(1)
        ?.replace("\"", "")
        ?.trim() ?: ""

    private fun MultipartBody.Part.getFileName(): String = headers()
        ?.value(0)
        ?.split(";")
        ?.get(2)
        ?.split("=")
        ?.get(1)
        ?.replace("\"", "")
        ?.trim() ?: ""

}