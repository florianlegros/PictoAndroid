package com.example.pictopicto.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.pictopicto.model.Question

@Dao
interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<Question>)

    @Query("SELECT * FROM Question")
    fun getAll(): LiveData<List<Question>>

    @Query("SELECT * FROM Question WHERE questionId=:id")
    suspend fun getQuestionById(id: Long): Question

    @Query("SELECT COUNT(*) from Question")
    suspend fun getCount(): Int

}
