package com.smart.autodaily.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.smart.autodaily.data.dao.ScriptInfoDao
import com.smart.autodaily.data.dao.ScriptSetInfoDao
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.ScriptSetInfo
import java.util.Locale


 var appDb: AppDb? = null

@Database(version = 1,
    exportSchema = false,
    entities = [ScriptInfo::class, ScriptSetInfo::class],
/*    autoMigrations = [
        AutoMigration(from = 2, to = 3)
    ]*/
)
abstract class AppDb  :  RoomDatabase(){
    abstract val scriptInfoDao : ScriptInfoDao
    abstract val scriptSetInfoDao : ScriptSetInfoDao
    companion object {
        // For Singleton instantiation

        private const val DATABASE_NAME = "auto_daily.db"

        fun getInstance(context: Context? = null): AppDb {
            return appDb ?: synchronized(this) {
                appDb
                    ?: buildDatabase(
                        context!!
                    ).also { appDb = it }
            }
        }

        private fun buildDatabase(context: Context): AppDb {
            //return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()
            return Room.databaseBuilder(context, AppDb::class.java, DATABASE_NAME)
                //.fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5, 6, 7, 8, 9)
                .addMigrations(*DatabaseMigrations.migrations)
                .allowMainThreadQueries()
                .addCallback(AppDb.dbCallback)
                .build()
        }

        private val dbCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.setLocale(Locale.CHINESE)
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
            }

        }
    }
}