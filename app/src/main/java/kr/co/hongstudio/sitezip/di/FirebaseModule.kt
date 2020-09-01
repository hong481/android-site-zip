package kr.co.hongstudio.sitezip.di

import kr.co.hongstudio.sitezip.firebase.FireBaseDatabaseUtil
import org.koin.core.module.Module
import org.koin.dsl.module

object FirebaseModule {

    @JvmStatic
    val INSTANCE: Module = module {
        single {
            FireBaseDatabaseUtil()
        }
    }
}