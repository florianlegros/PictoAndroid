package com.example.pictopicto.model

import androidx.room.Entity
import androidx.room.TypeConverters
import com.example.pictopicto.DataConverter
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "Categorie", primaryKeys = ["categorieId", "categorieNom"])
data class Categorie(
    @SerializedName("id")
    var categorieId: Long,

    @SerializedName("nom")
    var categorieNom: String,

    @SerializedName("imgfile")
    var categorieImgfile: String,

    @TypeConverters(DataConverter::class)
    @SerializedName("pictogrammes")
    var mots: List<Mot> = ArrayList()
) : Serializable