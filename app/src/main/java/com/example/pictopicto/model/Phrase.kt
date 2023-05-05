package com.example.pictopicto.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.pictopicto.DataConverter
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "Phrase")
data class Phrase(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    var phraseId: Long,

    @Embedded
    @SerializedName("question")
    var question: Question?,

    @TypeConverters(DataConverter::class)
    @SerializedName("pictogrammes")
    var pictogrammes: List<Pictogramme>
) : Serializable{
    constructor(question: Question?, pictogrammes: List<Pictogramme>) : this(0, question, pictogrammes)
}