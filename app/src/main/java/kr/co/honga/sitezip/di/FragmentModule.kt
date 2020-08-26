package kr.co.honga.sitezip.di

import kr.co.honga.sitezip.ui.screen.SiteZipFragment
import org.koin.androidx.fragment.dsl.fragment
import org.koin.core.module.Module
import org.koin.dsl.module

object FragmentModule {

    val INSTANCE: Module = module {
        fragment {
            SiteZipFragment()
        }
    }

}