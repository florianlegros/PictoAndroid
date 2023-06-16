package com.example.pictopicto.model

import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "Tag", primaryKeys = ["id", "nom"])
data class Tag(
    @SerializedName("id")
    var id: Long,

    @SerializedName("nom")
    var nom: String,

) : Serializable {
    constructor(nom: String) : this(0,nom)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Tag

        if (nom != other.nom) return false

        return true
    }

    override fun hashCode(): Int {
        return nom.hashCode()
    }
}