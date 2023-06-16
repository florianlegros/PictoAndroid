package com.example.pictopicto.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.pictopicto.api.ApiClient
import com.example.pictopicto.dao.AppDatabase
import com.example.pictopicto.model.Question
import com.example.pictopicto.model.Conjugaison
import com.example.pictopicto.payload.response.EmbeddedResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class ConjugaisonRepository private constructor(context: Context) {
    var conjugaisons: LiveData<List<Conjugaison>>
    private val bdd: AppDatabase

    private val allConjugaisons: LiveData<List<Conjugaison>>
        get() = bdd.conjugaisonDao()!!.getAll()

    suspend fun addAll(Conjugaisons: List<Conjugaison>) {
        bdd.conjugaisonDao()?.insertAll(Conjugaisons)
    }

    fun getAll(): LiveData<List<Conjugaison>>? {
        return bdd.conjugaisonDao()?.getAll()
    }

    suspend fun getConjugaisonById(ConjugaisonId: Long): Conjugaison? {
        return bdd.conjugaisonDao()?.getConjugaisonById(ConjugaisonId)
    }

    suspend fun insertConjugaison(Conjugaison: Conjugaison) {
        bdd.conjugaisonDao()?.insertConjugaison(Conjugaison)
    }

    suspend fun updateDatabase(context: Context) {
        val apiClient = ApiClient()
        var conjugaisons: ArrayList<Conjugaison> = ArrayList()
        apiClient.getApiService(context).getConjugaisons()
            .enqueue(object : retrofit2.Callback<EmbeddedResponse<Conjugaison>> {
                override fun onFailure(
                    call: Call<EmbeddedResponse<Conjugaison>>,
                    t: Throwable
                ) {
                    // Error fetching
                    println("erreur fetching")
                    println(t.printStackTrace())
                }

                override fun onResponse(
                    call: Call<EmbeddedResponse<Conjugaison>>,
                    response: Response<EmbeddedResponse<Conjugaison>>
                ) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val temp = response.body()
                        temp?.let { conjugaisons = it as ArrayList<Conjugaison> }
                        if (conjugaisons.size > 0) {
                            addAll(conjugaisons)
                        }
                    }
                }
            })
    }

    companion object {
        private var ourInstance: ConjugaisonRepository? = null
        fun getInstance(context: Context): ConjugaisonRepository? {
            if (ourInstance == null) {
                ourInstance = ConjugaisonRepository(context)
            }
            return ourInstance
        }
    }

    init {
        bdd = AppDatabase.getInstance(context)!!
        conjugaisons = allConjugaisons
    }
}
