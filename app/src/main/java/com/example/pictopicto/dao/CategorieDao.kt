package com.example.pictopicto.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.pictopicto.model.Categorie

@Dao
interface CategorieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategorie(categorie: Categorie)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<Categorie>)

    @Query("SELECT * FROM Categorie ORDER BY categorieNom ASC")
    fun getAll(): LiveData<List<Categorie>>

    @Query("SELECT * FROM Categorie WHERE categorieId=:id")
    suspend fun getCategorieById(id: Long): Categorie

    @Query("SELECT COUNT(*) from Categorie")
    suspend fun getCount(): Int

}
