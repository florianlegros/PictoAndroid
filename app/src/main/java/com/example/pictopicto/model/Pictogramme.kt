package com.example.pictopicto.model

import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "Pictogramme", primaryKeys = ["pictoId", "pictoNom"])
data class Pictogramme(
    @SerializedName("id")
    var pictoId: Long,

    @SerializedName("nom")
    var pictoNom: String,

    @SerializedName("imgfile")
    var pictoImgfile: String,

    @SerializedName("categorieId")
    var ecategorieId: Long
    ) : Serializable