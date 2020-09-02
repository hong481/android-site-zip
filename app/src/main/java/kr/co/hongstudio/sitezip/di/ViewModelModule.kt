package kr.co.hongstudio.sitezip.di

import androidx.lifecycle.SavedStateHandle
import kr.co.hongstudio.sitezip.ui.screen.MainViewModel
import kr.co.hongstudio.sitezip.ui.screen.SiteZipViewModel
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
                get()
            )
        }

        viewModel { (stateHandle: SavedStateHandle) ->
            SiteZipViewModel(
                stateHandle,
                get(),
                get(),
                get(),
                get()
            )
        }
    }
}
