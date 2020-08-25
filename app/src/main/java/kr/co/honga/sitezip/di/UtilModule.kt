package kr.co.honga.sitezip.di


import kr.co.honga.sitezip.util.*
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

object UtilModule {

    const val TAG: String = "UtilsModule"

    val INSTANCE: Module = module {
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
    }
}