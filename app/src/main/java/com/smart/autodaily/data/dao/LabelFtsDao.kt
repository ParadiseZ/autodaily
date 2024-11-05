package com.smart.autodaily.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.smart.autodaily.data.entity.LabelFts

@Dao
interface LabelFtsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun  insertAll(vararg items: LabelFts)

    @Insert
    fun  insertAll(labelFts: List<LabelFts>)

    @Query("insert into label_fts(id, label) select id,page_labels from script_action_info where page_labels is not null union select id,only_labels from script_action_info where only_labels is not null")
    fun  insertFrom()

    @RawQuery
    fun queryData(query: SupportSQLiteQuery): List<LabelFts>

    @Query("delete from label_fts")
    fun clearFtsTable()

    @Query("insert into label_fts(label_fts) values ('optimize')")
    fun optimizeFtsTable()

    @Query("select id from (SELECT t1.id as id, COUNT(t2.label) AS match_count " +
            "FROM label_fts t1 " +
            "JOIN label_temp t2 ON t1.label MATCH t2.label " +
            "GROUP BY t1.id " +
            "ORDER BY match_count DESC " +
            "LIMIT 1)")
    fun getMaxIdFromCurrent() :Int
}