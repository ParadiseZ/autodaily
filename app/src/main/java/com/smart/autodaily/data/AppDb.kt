package com.smart.autodaily.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.smart.autodaily.data.dao.ScriptInfoDao
import com.smart.autodaily.data.entity.ScriptInfo
import java.util.Locale

@Database(version = 1,
    exportSchema = false,
    entities = [ScriptInfo::class],
    autoMigrations = [
        AutoMigration(from = 2, to = 3)
    ])
abstract class AppDb  :  RoomDatabase(){
    abstract fun scriptInfoDao(): ScriptInfoDao
    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: AppDb? = null
        const val DATABASE_NAME = "auto_daily.db"

        fun getInstance(context: Context? = null): AppDb {
            return instance ?: synchronized(this) {
                instance
                    ?: buildDatabase(
                        context ?: throw IllegalStateException("Database has not been initialized.")
                    ).also { instance = it }
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
                /*val insertBookGroupAllSql = """
                    insert into book_groups(groupId, groupName, 'order', show) 
                    select ${BookGroup.IdAll}, '全部', -10, 1
                    where not exists (select * from book_groups where groupId = ${BookGroup.IdAll})
                """.trimIndent()
                db.execSQL(insertBookGroupAllSql)*/
            }
        }
    }
}