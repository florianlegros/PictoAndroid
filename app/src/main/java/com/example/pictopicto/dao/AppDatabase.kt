package com.example.pictopicto.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pictopicto.DataConverter
import com.example.pictopicto.model.Categorie
import com.example.pictopicto.model.Phrase
import com.example.pictopicto.model.Pictogramme
import com.example.pictopicto.model.Question

@Database(
    entities = [Pictogramme::class, Categorie::class, Question::class, Phrase::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    DataConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pictogrammeDao(): PictogrammeDao?
    abstract fun categorieDao(): CategorieDao?
    abstract fun questionDao(): QuestionDao?
    abstract fun phraseDao(): PhraseDao?

    companion object {
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "gestion.db"
                    ).build()
                }
            }
            return INSTANCE
        }


    }
}


