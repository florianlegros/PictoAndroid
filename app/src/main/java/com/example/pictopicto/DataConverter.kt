package com.example.pictopicto

import androidx.room.TypeConverter
import com.example.pictopicto.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DataConverter {

    @TypeConverter
    fun fromPictogrammeLangList(value: List<Mot>?): String {
        val gson = Gson()
        val type = object : TypeToken<List<Mot>>() {}.type
        return gson.toJson(value, type) ?: ""
    }

    @TypeConverter
    fun toPictogrammeLangList(value: String): List<Mot> {
        val gson = Gson()
        val type = object : TypeToken<List<Mot>>() {}.type
        return gson.fromJson(value, type) ?: ArrayList()
    }

    @TypeConverter
    fun fromCategorieLangList(value: List<Categorie>?): String {
        val gson = Gson()
        val type = object : TypeToken<List<Categorie>>() {}.type
        return gson.toJson(value, type) ?: ""
    }

    @TypeConverter
    fun toCategorieLangList(value: String): List<Categorie> {
        val gson = Gson()
        val type = object : TypeToken<List<Categorie>>() {}.type
        return gson.fromJson<List<Categorie>>(value, type) ?: ArrayList()
    }

    @TypeConverter
    fun fromTagLangList(value: List<Tag>?): String {
        val gson = Gson()
        val type = object : TypeToken<List<Tag>>() {}.type
        return gson.toJson(value, type) ?: ""
    }

    @TypeConverter
    fun toTagLangList(value: String): List<Tag> {
        val gson = Gson()
        val type = object : TypeToken<List<Tag>>() {}.type
        return gson.fromJson<List<Tag>>(value, type) ?: ArrayList()
    }

    @TypeConverter
    fun fromConjugaisonLangList(value: List<Conjugaison>?): String {
        val gson = Gson()
        val type = object : TypeToken<List<Conjugaison>>() {}.type
        return gson.toJson(value, type) ?: ""
    }

    @TypeConverter
    fun toConjugaisonLangList(value: String): List<Conjugaison> {
        val gson = Gson()
        val type = object : TypeToken<List<Conjugaison>>() {}.type
        return gson.fromJson<List<Conjugaison>>(value, type) ?: ArrayList()
    }

    @TypeConverter
    fun fromIrregulierLang(value: Irregulier?): String {
        val gson = Gson()
        val type = object : TypeToken<Irregulier>() {}.type
        return gson.toJson(value, type) ?: ""
    }

    @TypeConverter
    fun toIrregulierLang(value: String): Irregulier? {
        val gson = Gson()
        val type = object : TypeToken<Irregulier>() {}.type
        return gson.fromJson<Irregulier>(value, type) ?: null
    }
}