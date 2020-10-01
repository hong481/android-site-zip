package kr.co.hongstudio.sitezip.di

import kr.co.hongstudio.sitezip.data.remote.PlaceApi
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

object ApiModule {

    val INSTANCE: Module = module {
        single {
            createApi<PlaceApi>(
                get(named(NetworkModule.Server.NAVER_SMART_STORE.tag))
            )
        }
    }

    private inline fun <reified T> createApi(retrofit: Retrofit): T =
        retrofit.create(T::class.java)

}