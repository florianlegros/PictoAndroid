package com.example.pictopicto.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.pictopicto.model.Irregulier

@Dao
interface IrregulierDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIrregulier(irregulier: Irregulier)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(irreguliers: List<Irregulier>)

    @Query("SELECT * FROM Irregulier")
    fun getAll(): LiveData<List<Irregulier>>

    @Query("SELECT * FROM Irregulier WHERE Id=:id")
    suspend fun getIrregulierById(id: Long): Irregulier

    @Query("SELECT COUNT(*) from Irregulier")
    suspend fun getCount(): Int

}
