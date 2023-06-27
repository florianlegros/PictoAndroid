package com.example.pictopicto.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.pictopicto.model.Mot

@Dao
interface MotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMot(mot: Mot)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mots: List<Mot>)

    @Query("SELECT * FROM mot")
    fun getAll(): LiveData<List<Mot>>

    @Query("SELECT * FROM mot WHERE pictoId=:id")
    suspend fun getMotById(id: Long): Mot

    @Query("SELECT * FROM mot WHERE categorieId=:id ORDER BY pictoId")
    suspend fun getAllMotByCategorieId(id: Long): List<Mot>

    @Query("SELECT COUNT(*) from mot")
    suspend fun getCount(): Int

}
