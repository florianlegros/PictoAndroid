package com.example.pictopicto.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.pictopicto.api.ApiClient
import com.example.pictopicto.dao.AppDatabase
import com.example.pictopicto.model.Question
import com.example.pictopicto.payload.response.EmbeddedResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class QuestionRepository private constructor(context: Context) {
    var questions: LiveData<List<Question>>
    private val bdd: AppDatabase

    private val allQuestions: LiveData<List<Question>>
        get() = bdd.questionDao()!!.getAll()

    suspend fun addAll(Questions: List<Question>) {
        bdd.questionDao()?.insertAll(Questions)
    }

    fun getAll(): LiveData<List<Question>>? {
        return bdd.questionDao()?.getAll()
    }

    suspend fun getQuestionById(QuestionId: Long): Question? {
        return bdd.questionDao()?.getQuestionById(QuestionId)
    }

    suspend fun insertQuestion(Question: Question) {
        bdd.questionDao()?.insertQuestion(Question)
    }

    suspend fun updateDatabase(context: Context) {
        val apiClient = ApiClient()
        var Questions: ArrayList<Question>
        apiClient.getApiService(context).getQuestions()
            .enqueue(object : retrofit2.Callback<EmbeddedResponse<Question>> {
                override fun onFailure(
                    call: Call<EmbeddedResponse<Question>>,
                    t: Throwable
                ) {
                    // Error fetching
                    println("erreur fetching")
                    println(t.printStackTrace())
                }

                override fun onResponse(
                    call: Call<EmbeddedResponse<Question>>,
                    response: Response<EmbeddedResponse<Question>>
                ) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val temp = response.body()
                        Questions = temp?.data as ArrayList<Question>
                        if (Questions.size > 0) {
                            addAll(Questions)
                        }
                    }
                }
            })
    }

    companion object {
        private var ourInstance: QuestionRepository? = null
        fun getInstance(context: Context): QuestionRepository? {
            if (ourInstance == null) {
                ourInstance = QuestionRepository(context)
            }
            return ourInstance
        }
    }

    init {
        bdd = AppDatabase.getInstance(context)!!
        questions = allQuestions
    }
}
