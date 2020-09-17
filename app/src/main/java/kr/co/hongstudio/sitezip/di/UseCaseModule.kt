package kr.co.hongstudio.sitezip.di

import kr.co.hongstudio.sitezip.domain.GetPlacesUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

object UseCaseModule {

    @JvmStatic
    val INSTANCE: Module = module {
        factory {
            GetPlacesUseCase(
                get(),
                get()
            )
        }
    }

}