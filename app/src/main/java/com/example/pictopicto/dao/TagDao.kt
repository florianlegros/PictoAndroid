package com.example.pictopicto.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.pictopicto.model.Tag

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tags: List<Tag>)

    @Query("SELECT * FROM Tag")
    fun getAll(): LiveData<List<Tag>>

    @Query("SELECT * FROM Tag WHERE Id=:id")
    suspend fun getTagById(id: Long): Tag

    @Query("SELECT COUNT(*) from Tag")
    suspend fun getCount(): Int

}
