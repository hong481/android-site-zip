package kr.co.honga.sitezip.di

import android.content.Context
import com.squareup.moshi.Moshi
import kr.co.honga.sitezip.App
import kr.co.honga.sitezip.BuildConfig
import kr.co.honga.sitezip.data.BuildProperty
import kr.co.honga.sitezip.util.extension.fromJson
import org.koin.core.module.Module
import org.koin.dsl.module

object AppModule {

    @JvmStatic
    val INSTANCE: Module = module {
        single {
            get<Context>() as App
        }
        single {
            get<Moshi>().fromJson<BuildProperty>(BuildConfig.BUILD_PROPERTIES)
        }
    }

    fun getModules(): List<Module> = mutableListOf(
        INSTANCE,
        UtilModule.INSTANCE,
        DataBaseModule.INSTANCE,
        FragmentModule.INSTANCE,
        ViewModelModule.INSTANCE,
        FirebaseModule.INSTANCE
    )
}