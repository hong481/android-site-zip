package kr.co.honga.sitezip.di

import androidx.lifecycle.SavedStateHandle
import kr.co.honga.sitezip.ui.screen.MainViewModel
import kr.co.honga.sitezip.ui.screen.SiteZipViewModel
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
                get()
            )
        }

        viewModel { (stateHandle: SavedStateHandle) ->
            SiteZipViewModel(
                stateHandle,
                get(),
                get(),
                get()
            )
        }
    }
}
