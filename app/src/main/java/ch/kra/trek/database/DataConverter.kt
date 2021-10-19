package ch.kra.trek.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class DataConverter {
    @TypeConverter
    fun fromCoordinateList(value: List<Coordinate>): String {
        val gson = Gson()
        val type = object: TypeToken<List<Coordinate>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toCoordinateList(value: String): List<Coordinate> {
        val gson= Gson()
        val type = object: TypeToken<List<Coordinate>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromTimestampToDate(value: Long): Date {
        return Date(value)
    }

    @TypeConverter
    fun fromDateToTimestamp(value: Date): Long {
        return value.time
    }
}