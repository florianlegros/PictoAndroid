package com.example.pictopicto.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.pictopicto.DataConverter
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "Question")
data class Question(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    var questionId: Long,

    @SerializedName("contenu")
    var contenu: String,

    @TypeConverters(DataConverter::class)
    @SerializedName("categories")
    var categories: List<Categorie>
) : Serializable {
    override fun toString(): String {
        return contenu
    }
}