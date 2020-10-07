package kr.co.hongstudio.sitezip.di

import kr.co.hongstudio.sitezip.data.local.preference.*
import org.koin.core.module.Module
import org.koin.dsl.module

@Suppress("USELESS_CAST")
object PreferenceModule {

    val INSTANCE: Module = module {
        single {
            BillingPreferenceImpl(
                get()
            ) as BillingPreference
        }
        single {
            AdMobPreferenceImpl(
                get()
            ) as AdMobPreference
        }
        single {
            AppPreferenceImpl(
                get()
            ) as AppPreference
        }
    }
}