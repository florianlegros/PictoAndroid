package com.example.pictopicto.model

import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "Conjugaison", primaryKeys = ["id"])
data class Conjugaison(
    @SerializedName("id")
    var id: Long,

    @SerializedName("temps")
    var temps: String,

    @SerializedName("premiere_pers_sing")
    var premiere_pers_sing: String,

    @SerializedName("deuxieme_pers_sing")
    var deuxieme_pers_sing: String,

    @SerializedName("troisieme_pers_sing")
    var troisieme_pers_sing: String,

    @SerializedName("premiere_pers_pluriel")
    var premiere_pers_pluriel: String,
    @SerializedName("deuxieme_pers_pluriel")
    var deuxieme_pers_pluriel: String,

    @SerializedName("troisieme_pers_pluriel")
    var troisieme_pers_pluriel: String


) : Serializable {
}