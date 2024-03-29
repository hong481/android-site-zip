package kr.co.hongstudio.sitezip.di

import kr.co.hongstudio.sitezip.admob.AdMobManager
import org.koin.core.module.Module
import org.koin.dsl.module

object AdMobModule {

    @JvmStatic
    val INSTANCE: Module = module {
        single {
            AdMobManager(
                get(),
                get()
            )
        }
    }
}