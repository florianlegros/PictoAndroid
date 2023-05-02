package com.example.pictopicto

import androidx.room.TypeConverter
import com.example.pictopicto.model.Pictogramme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DataConverter {

    @TypeConverter
    fun fromPictogrammeLangList(value: List<Pictogramme>): String {
        val gson = Gson()
        val type = object : TypeToken<List<Pictogramme>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toPictogrammeLangList(value: String): List<Pictogramme> {
        val gson = Gson()
        val type = object : TypeToken<List<Pictogramme>>() {}.type
        return gson.fromJson(value, type)
    }
}