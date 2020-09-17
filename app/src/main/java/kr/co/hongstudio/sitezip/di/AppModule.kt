package kr.co.hongstudio.sitezip.di

import android.content.Context
import com.squareup.moshi.Moshi
import kr.co.hongstudio.sitezip.App
import kr.co.hongstudio.sitezip.BuildConfig
import kr.co.hongstudio.sitezip.data.BuildProperty
import kr.co.hongstudio.sitezip.util.extension.fromJson
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
        MoshiModule.INSTANCE,
        ObserverModule.INSTANCE,
        PreferenceModule.INSTANCE,
        UtilModule.INSTANCE,
        DataBaseModule.INSTANCE,
        NetworkModule.INSTANCE,
        ApiModule.INSTANCE,
        UseCaseModule.INSTANCE,
        FragmentModule.INSTANCE,
        ViewModelModule.INSTANCE,
        FirebaseModule.INSTANCE,
        AdMobModule.INSTANCE,
        BillingModule.INSTANCE
    )
}