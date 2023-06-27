package com.example.pictopicto.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.pictopicto.api.ApiClient
import com.example.pictopicto.dao.AppDatabase
import com.example.pictopicto.model.Phrase
import com.example.pictopicto.payload.request.EmbeddedRequest
import com.example.pictopicto.payload.response.EmbeddedResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class PhraseRepository private constructor(context: Context) {
    var phrases: LiveData<List<Phrase>>
    private val bdd: AppDatabase

    private val allPhrases: LiveData<List<Phrase>>
        get() = bdd.phraseDao()!!.getAll()

    suspend fun addAll(phrases: List<Phrase>) {
        bdd.phraseDao()?.insertAll(phrases)
    }

    fun getAll() {
        bdd.phraseDao()?.getAll()
    }

    suspend fun getPhraseById(phraseId: Long): Phrase? {
        return bdd.phraseDao()?.getPhraseById(phraseId)
    }

    suspend fun insertPhrase(phrase: Phrase) {
        bdd.phraseDao()?.insertPhrase(phrase)
    }

    suspend fun updateDatabase(context: Context) {
        val apiClient = ApiClient()
        var phrases: ArrayList<Phrase>
        apiClient.getApiService(context).getPhrases()
            .enqueue(object : retrofit2.Callback<EmbeddedResponse<Phrase>> {
                override fun onFailure(
                    call: Call<EmbeddedResponse<Phrase>>,
                    t: Throwable
                ) {
                    // Error fetching
                    println("erreur fetching")
                    println(t.printStackTrace())
                }

                override fun onResponse(
                    call: Call<EmbeddedResponse<Phrase>>,
                    response: Response<EmbeddedResponse<Phrase>>
                ) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val temp = response.body()
                        phrases =
                            if (temp?.data != null) temp.data as ArrayList<Phrase> else ArrayList()
                        phrases.removeIf { it.mots.isEmpty() }
                        if (phrases.size > 0) {
                            addAll(phrases)
                        }
                    }
                }
            })

        bdd.phraseDao()?.findAll()?.forEach { phrase ->
            if (phrase.phraseId > 0 && phrase.mots.isEmpty().not()) {
                apiClient.getApiService(context).addPhrase(EmbeddedRequest(phrase))
                    .enqueue(object : retrofit2.Callback<EmbeddedResponse<Phrase>> {
                        override fun onResponse(
                            call: Call<EmbeddedResponse<Phrase>>,
                            response: Response<EmbeddedResponse<Phrase>>
                        ) {
                            println("=====================$response")
                        }

                        override fun onFailure(call: Call<EmbeddedResponse<Phrase>>, t: Throwable) {
                            println("erreur posting")
                            println(t.printStackTrace())
                        }

                    })
            }
        }

    }

    companion object {
        private var ourInstance: PhraseRepository? = null
        fun getInstance(context: Context): PhraseRepository? {
            if (ourInstance == null) {
                ourInstance = PhraseRepository(context)
            }
            return ourInstance
        }
    }

    init {
        bdd = AppDatabase.getInstance(context)!!
        phrases = allPhrases
    }
}
