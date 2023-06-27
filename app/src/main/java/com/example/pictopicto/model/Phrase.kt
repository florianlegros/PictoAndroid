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

    var mots: List<Mot>

) : Serializable {
    constructor(question: Question?, mots: List<Mot>) : this(
        0,
        question,
        mots
    )
}