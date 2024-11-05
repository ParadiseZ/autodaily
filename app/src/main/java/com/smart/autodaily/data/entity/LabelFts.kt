package com.smart.autodaily.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions

@Fts4(tokenizer = FtsOptions.TOKENIZER_SIMPLE)
@Entity(tableName = "label_fts")
data class LabelFts(
    @ColumnInfo(name = "id") val id : Int,
    @ColumnInfo(name = "label") val label: String
)