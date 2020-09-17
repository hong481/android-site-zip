package kr.co.hongstudio.sitezip.di

import android.annotation.SuppressLint
import com.squareup.moshi.Moshi
import kr.co.hongstudio.sitezip.util.DirectoryManager
import kr.co.hongstudio.sitezip.util.http.OkHttpInterceptor
import kr.co.irlink.irsdk.network.http.OkHttpUtil
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Protocol
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

object NetworkModule {

    enum class Server(
        val tag: String,
        val url: String
    ) {
        NAVER_SMART_STORE("NAVER_SMART_STORE", "http://store.naver.com"),
    }

    private const val TIME_OUT: Long = 1000 * 60 * 10

    private const val CACHE_SIZE: Long = 10 * 1024 * 1024

    private val hostNameVerifier: HostnameVerifier = HostnameVerifier { _, _ ->
        true
    }

    private val trustManager: X509TrustManager = object : X509TrustManager {

        @SuppressLint("TrustAllX509TrustManager")
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            // ..
        }

        @SuppressLint("TrustAllX509TrustManager")
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            // ..
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

    private val sslSocketFactory: SSLSocketFactory = SSLContext.getInstance("TLS").run {
        init(null, arrayOf(trustManager), null)
        socketFactory
    }

    @JvmStatic
    val INSTANCE: Module = module {
        single {
            OkHttpUtil()
        }
        single {
            OkHttpInterceptor()
        }
        single {
            createOkHttp(
                get<DirectoryManager>().okHttpDir,
                get()
            )
        }
        single(named(Server.NAVER_SMART_STORE.tag)) {
            createRetrofit(
                Server.NAVER_SMART_STORE.url,
                get(),
                get()
            )
        }
    }

    @JvmStatic
    private fun createOkHttp(cacheDir: File, interceptor: OkHttpInterceptor): OkHttpClient = OkHttpClient.Builder()
        .protocols(listOf(Protocol.HTTP_1_1))
        .hostnameVerifier(hostNameVerifier)
        .sslSocketFactory(sslSocketFactory, trustManager)
        .readTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
        .writeTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
        .connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
        .cache(Cache(cacheDir, CACHE_SIZE))
        .addInterceptor(interceptor)
        .build()


    @JvmStatic
    private fun createRetrofit(baseUrl: String, okHttpClient: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .client(okHttpClient)
        .build()

}