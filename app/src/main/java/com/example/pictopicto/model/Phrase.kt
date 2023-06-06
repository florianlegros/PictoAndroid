package com.example.pictopicto.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
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

    var pictogrammes: List<Pictogramme>

) : Serializable {
    constructor(question: Question?, pictogrammes: List<Pictogramme>) : this(
        0,
        question,
        pictogrammes
    )
}