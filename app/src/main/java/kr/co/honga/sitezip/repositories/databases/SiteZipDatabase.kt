package kr.co.honga.sitezip.repositories.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import kr.co.honga.sitezip.data.local.entity.Site
import kr.co.honga.sitezip.repositories.daos.SiteDao

@Database(entities = [Site::class], version = 1)
abstract class SiteZipDatabase : RoomDatabase() {
    abstract fun siteDao(): SiteDao
}