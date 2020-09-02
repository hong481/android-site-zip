package kr.co.hongstudio.sitezip.di

import kr.co.hongstudio.sitezip.observer.NetworkObserver
import org.koin.core.module.Module
import org.koin.dsl.module

object ObserverModule {

    val INSTANCE: Module = module {
        single {
            NetworkObserver(
                get()
            )
        }
    }

}