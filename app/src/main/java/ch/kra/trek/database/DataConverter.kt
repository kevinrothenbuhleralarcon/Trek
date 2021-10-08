package ch.kra.trek.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
}