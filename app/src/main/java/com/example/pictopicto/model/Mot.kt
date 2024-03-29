package com.example.pictopicto.model

import androidx.room.Entity
import androidx.room.TypeConverters
import com.example.pictopicto.DataConverter
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "Mot", primaryKeys = ["pictoId", "pictoNom"])
data class Mot(
    @SerializedName("id")
    var pictoId: Long,

    @SerializedName("nom")
    var pictoNom: String,

    @SerializedName("imgfile")
    var pictoImgfile: String,

    @SerializedName("categorieId")
    var categorieId: Long,

    @TypeConverters(DataConverter::class)
    @SerializedName("tags")
    var tags: List<Tag>,

    @TypeConverters(DataConverter::class)
    @SerializedName("irregulier")
    var irregulier:Irregulier?


) : Serializable {
}