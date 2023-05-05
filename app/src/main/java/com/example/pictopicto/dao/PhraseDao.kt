package com.example.pictopicto.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.pictopicto.model.Phrase

@Dao
interface PhraseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrase(phrase: Phrase)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(phrases: List<Phrase>)

    @Query("SELECT * FROM phrase")
    fun getAll(): LiveData<List<Phrase>>

    @Query("SELECT * FROM phrase")
    fun findAll(): List<Phrase>

    @Query("SELECT * FROM phrase WHERE phraseId=:id")
    suspend fun getPhraseById(id: Long): Phrase

    @Query("SELECT COUNT(*) from phrase")
    suspend fun getCount(): Int

}
