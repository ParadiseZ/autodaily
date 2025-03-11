package com.smart.autodaily.data.entity.convert

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.smart.autodaily.command.Command
import com.smart.autodaily.data.entity.Point

object CustomTypeConvert {
    private val gson = Gson()
    //arraylist
    @TypeConverter
    fun toStringConvert(value: ArrayList<String>): String {
        return gson.toJson(value)
    }
    @TypeConverter
    fun toArrayList(data : String): ArrayList<String> {
        return gson.fromJson(data,Array<String>::class.java).toMutableList() as ArrayList
    }
    //command
    @TypeConverter
    fun toStringConvert(value: Command): String {
        return gson.toJson(value)
    }
    @TypeConverter
    fun toCommand(value: String): ArrayList<Command> {
        return gson.fromJson(value, Array<Command>::class.java).toMutableList() as ArrayList
    }
    //Point
    @TypeConverter
    fun toStringConvert(point: Point): String {
        return "${point.x},${point.y}"
    }
    @TypeConverter
    fun toPoint(data: String): Point {
        val (x, y) = data.split(",").map { it.toFloat() }
        return Point(x, y)
    }
}