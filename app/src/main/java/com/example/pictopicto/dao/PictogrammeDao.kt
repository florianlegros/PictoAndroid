package com.example.pictopicto.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.pictopicto.model.Pictogramme

@Dao
interface PictogrammeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPictogramme(pictogramme: Pictogramme)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pictogrammes: List<Pictogramme>)

    @Query("SELECT * FROM pictogramme")
    fun getAll(): LiveData<List<Pictogramme>>

    @Query("SELECT * FROM pictogramme WHERE pictoId=:id")
    suspend fun getPictogrammeById(id: Long): Pictogramme

    @Query("SELECT * FROM pictogramme WHERE categorieId=:id ORDER BY pictoId")
    suspend fun getAllPictogrammeByCategorieId(id: Long): List<Pictogramme>

    @Query("SELECT COUNT(*) from pictogramme")
    suspend fun getCount(): Int

}
