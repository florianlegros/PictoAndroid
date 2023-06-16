package com.example.pictopicto.model

import androidx.room.Entity
import androidx.room.Relation
import androidx.room.TypeConverters
import com.example.pictopicto.DataConverter
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "Irregulier", primaryKeys = ["id"])
data class Irregulier(
    @SerializedName("id")
    var id: Long,

    @SerializedName("feminin")
    var feminin: String,

    @SerializedName("participePasse")
    var participePasse: String,

    @SerializedName("pluriel")
    var pluriel: String,

    @TypeConverters(DataConverter::class)
    @SerializedName("conjugaisons")
    var conjugaison:List<Conjugaison>

) : Serializable {
}