package kr.co.hongstudio.sitezip.di


import kr.co.hongstudio.sitezip.util.*
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

object UtilModule {

    const val TAG: String = "UtilsModule"

    val INSTANCE: Module = module {
        single {
            PermissionUtil()
        }
        single {
            ResourceProviderImpl(
                get()
            )
        } bind ResourceProvider::class

        single {
            ActivityUtil(
                get()
            )
        }

        single {
            ClipboardUtil(
                get()
            )
        }

        single {
            DirectoryManagerImpl(
                get(),
                get()
            )
        } bind DirectoryManager::class

        single {
            FileUtil()
        }

        single {
            ValidatorUtil()
        }

        single {
            UrlParserUtil(
                get()
            )
        }

        single {
            DisplayUtil(
                get()
            )
        }

        single {
            LocationUtil(
                get()
            )
        }
    }
}