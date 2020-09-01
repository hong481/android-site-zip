package kr.co.hongstudio.sitezip.di

import kr.co.hongstudio.sitezip.billing.BillingManager
import org.koin.core.module.Module
import org.koin.dsl.module

object BillingModule {

    @JvmStatic
    val INSTANCE: Module = module {
        single {
            BillingManager(
                get(),
                get()
            )
        }
    }
}