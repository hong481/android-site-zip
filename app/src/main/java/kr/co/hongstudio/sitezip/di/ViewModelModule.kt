package kr.co.hongstudio.sitezip.di

import androidx.lifecycle.SavedStateHandle
import kr.co.hongstudio.sitezip.ui.appirater.AppiraterDialogViewModel
import kr.co.hongstudio.sitezip.ui.screen.MainViewModel
import kr.co.hongstudio.sitezip.ui.screen.place.PlaceZipViewModel
import kr.co.hongstudio.sitezip.ui.screen.setting.SettingViewModel
import kr.co.hongstudio.sitezip.ui.screen.site.SiteZipViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

object ViewModelModule {

    @JvmStatic
    val INSTANCE: Module = module {
        viewModel {
            MainViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get()
            )
        }

        viewModel { (stateHandle: SavedStateHandle) ->
            SiteZipViewModel(
                stateHandle,
                get(),
                get(),
                get(),
                get(),
                get()
            )
        }

        viewModel {
            PlaceZipViewModel(
                get(),
                get(),
                get()
            )
        }

        viewModel {
            SettingViewModel()
        }

        viewModel {
            AppiraterDialogViewModel(
                get(),
                get()
            )
        }
    }
}
