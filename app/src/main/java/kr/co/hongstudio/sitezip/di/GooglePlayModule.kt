package kr.co.hongstudio.sitezip.di

import kr.co.hongstudio.sitezip.googleplay.InAppReviewManager
import org.koin.core.module.Module
import org.koin.dsl.module

object GooglePlayModule {

    @JvmStatic
    val INSTANCE: Module = module {
        single {
            InAppReviewManager(
                get()
            )
        }
    }

}