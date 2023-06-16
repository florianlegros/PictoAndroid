package com.example.pictopicto.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.pictopicto.model.Conjugaison

@Dao
interface ConjugaisonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConjugaison(conjugaison: Conjugaison)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(conjugaisons: List<Conjugaison>)

    @Query("SELECT * FROM Conjugaison")
    fun getAll(): LiveData<List<Conjugaison>>

    @Query("SELECT * FROM Conjugaison WHERE Id=:id")
    suspend fun getConjugaisonById(id: Long): Conjugaison

    @Query("SELECT COUNT(*) from Conjugaison")
    suspend fun getCount(): Int

}
