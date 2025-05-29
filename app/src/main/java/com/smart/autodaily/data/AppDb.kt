package com.smart.autodaily.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.smart.autodaily.data.AppDb.Companion.DATABASE_NAME
import com.smart.autodaily.data.dao.AppInfoDao
import com.smart.autodaily.data.dao.ScriptActionInfoDao
import com.smart.autodaily.data.dao.ScriptInfoDao
import com.smart.autodaily.data.dao.ScriptRunStatusDao
import com.smart.autodaily.data.dao.ScriptSetInfoDao
import com.smart.autodaily.data.dao.ScriptSetRunStatusDao
import com.smart.autodaily.data.dao.UserInfoDao
import com.smart.autodaily.data.entity.AppInfo
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.data.entity.ScriptInfo
import com.smart.autodaily.data.entity.ScriptRunStatus
import com.smart.autodaily.data.entity.ScriptSetInfo
import com.smart.autodaily.data.entity.ScriptSetRunStatus
import com.smart.autodaily.data.entity.UserInfo
import splitties.init.appCtx
import java.util.Locale


val appDb by lazy {
    Room.databaseBuilder(appCtx, AppDb::class.java, DATABASE_NAME)
        //.fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5, 6, 7, 8, 9)
        .addMigrations(*DatabaseMigrations.migrations)
        .allowMainThreadQueries()
        .addCallback(AppDb.dbCallback)
        .build()
}
@Database(version = 2,
    exportSchema = true,
    entities = [
        ScriptInfo::class,
        ScriptSetInfo::class,
        UserInfo::class,
        ScriptActionInfo::class,
        ScriptSetRunStatus::class,
        ScriptRunStatus::class,
        AppInfo::class],
        autoMigrations = [
            AutoMigration(from = 1, to = 2)
        ]
)
abstract class AppDb  :  RoomDatabase(){
    abstract val scriptInfoDao : ScriptInfoDao
    abstract val scriptSetInfoDao : ScriptSetInfoDao
    abstract val userInfoDao : UserInfoDao
    abstract val scriptActionInfoDao : ScriptActionInfoDao
    abstract val appInfoDao : AppInfoDao
    abstract val scriptSetRunStatusDao : ScriptSetRunStatusDao
    abstract val scriptRunStatusDao : ScriptRunStatusDao

    companion object {
        // For Singleton instantiation

        const val DATABASE_NAME = "auto_daily.db"

        val dbCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.setLocale(Locale.CHINESE)
                //initData(db)
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
            }

        }
/*
        private fun initData(db: SupportSQLiteDatabase){
            *//*@Language("sql")
            val insertScriptSetGlobalSql = """
                INSERT INTO script_set_info 
                (script_id, set_id, checked_flag, set_type, set_name, set_desc, set_parent_id, set_level, set_default_value, set_value, set_range, set_step, is_show, result_flag, once_flag) 
                VALUES 
                    (0,1,'true','CHECK_BOX','启动时打开脚本设置','',-1,1,'true','true','',0,'true','false','false'),
                    (0,2,'true','CHECK_BOX','保持运行','',-1,1,'false','false','',0,'true','false','false'),
                    (0,3,'true','SLIDER_SECOND','截图和操作延迟','',-1,1,'0.8','0.8','0,3',15,'true','false','false'),
                    (0,4,'true','SLIDER_SECOND','卡死等待重启','',-1,1,'10','10','5,20',14,'true','false','false'),
                    (0,5,'true','SLIDER','相似度最小值','',-1,1,'0.60','0.60','0,1',0,'true','false','false'),
                    (0,6,'true','RADIO_BUTTON','匹配算法','',-1,1,'算法一（更快）,算法二（更准）','算法二（更准）','',0,'true','false','false'),
                    (0,7,'true','RADIO_BUTTON','脚本日志记录','',-1,1,'关闭,运行结果,INFO','运行结果','',0,'true','false','false'),
                    (0,8,'true','RADIO_BUTTON','工作模式','',-1,1,'无障碍+录屏(安卓7-12),shizuku,root','shizuku','',0,'true','false','false'),
                    (0,9,'true','SLIDER_THIRD','随机点击范围','',-1,1,'5','5','1,10',9,'true','false','false')
                    (0,10,'true','CHECK_BOX','使用gpu推理','',-1,1,'true','true','','','true','false','false')
            """.trimIndent()
            db.execSQL(insertScriptSetGlobalSql)*//*
        }*/
    }
}