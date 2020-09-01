package kr.co.hongstudio.sitezip.di

import androidx.room.Room
import androidx.room.RoomDatabase
import kr.co.hongstudio.sitezip.repositories.databases.SiteZipDatabase
import kr.co.hongstudio.sitezip.repositories.repository.SiteRepository
import kr.co.hongstudio.sitezip.repositories.repository.SiteRepositoryImpl
import kr.co.hongstudio.sitezip.util.DatabaseDebugUtil
import kr.co.hongstudio.sitezip.util.DirectoryManagerImpl
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import java.io.File

object DataBaseModule {

    /**
     * 데이터 베이스 파일명.
     */
    private const val DATABASE_NAME: String = "sitezip_database"

    const val TAG: String = "DataBaseModule"

    @JvmStatic
    val INSTANCE: Module = module {
        single {
            Room.databaseBuilder(
                get(),
                SiteZipDatabase::class.java,
                "${get<DirectoryManagerImpl>().rootDir}/$DATABASE_NAME"
            ).setJournalMode(RoomDatabase.JournalMode.AUTOMATIC)
                .build()
        }

        single {
            get<SiteZipDatabase>().siteDao()
        }

        single {
            SiteRepositoryImpl(
                get()
            )
        } bind SiteRepository::class

        single {
            DatabaseDebugUtil(
                File(get<DirectoryManagerImpl>().rootDir, DATABASE_NAME)
            )
        }
    }
}