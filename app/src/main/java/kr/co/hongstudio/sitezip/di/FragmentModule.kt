package kr.co.hongstudio.sitezip.di

import kr.co.hongstudio.sitezip.ui.screen.place.PlaceZipFragment
import kr.co.hongstudio.sitezip.ui.screen.setting.SettingFragment
import kr.co.hongstudio.sitezip.ui.screen.site.SiteZipFragment
import org.koin.androidx.fragment.dsl.fragment
import org.koin.core.module.Module
import org.koin.dsl.module

object FragmentModule {

    val INSTANCE: Module = module {
        fragment {
            SiteZipFragment()
        }

        fragment {
            PlaceZipFragment()
        }

        fragment {
            SettingFragment()
        }

    }

}