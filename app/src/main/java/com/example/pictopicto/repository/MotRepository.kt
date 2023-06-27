package com.example.pictopicto.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.pictopicto.api.ApiClient
import com.example.pictopicto.dao.AppDatabase
import com.example.pictopicto.model.Mot
import com.example.pictopicto.payload.response.EmbeddedResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class MotRepository private constructor(context: Context) {
    var pictogrammes: LiveData<List<Mot>>
    var pictogrammesByCategorie: MutableLiveData<List<Mot>> = MutableLiveData()
    private val bdd: AppDatabase
    var categorieId: Long = 17

    private val allPictogrammes: LiveData<List<Mot>>
        get() = bdd.pictogrammeDao()!!.getAll()

    suspend fun addAll(mots: List<Mot>) {
        bdd.pictogrammeDao()?.insertAll(mots)
    }

    fun getAll() {
        bdd.pictogrammeDao()?.getAll()
    }

    suspend fun getPictogrammeById(pictogrammeId: Long): Mot? {
        return bdd.pictogrammeDao()?.getMotById(pictogrammeId)
    }

    suspend fun getAllPictogrammeByCategorieId(categorieId: Long): List<Mot>? {
        val temp = bdd.pictogrammeDao()?.getAllMotByCategorieId(categorieId)
        pictogrammesByCategorie.postValue(temp)
        return temp
    }

    suspend fun insertPictogramme(mot: Mot) {
        bdd.pictogrammeDao()?.insertMot(mot)
    }

    suspend fun updateDatabase(context: Context) {
        val apiClient = ApiClient()
        var mots: ArrayList<Mot> = ArrayList()
        apiClient.getApiService(context).getMots()
            .enqueue(object : retrofit2.Callback<List<Mot>> {
                override fun onFailure(
                    call: Call<List<Mot>>,
                    t: Throwable
                ) {
                    // Error fetching
                    println("erreur fetching")
                    println(t.printStackTrace())
                }

                override fun onResponse(
                    call: Call<List<Mot>>,
                    response: Response<List<Mot>>
                ) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val temp = response.body()
                        temp?.let { mots = it as ArrayList<Mot> }
                        if (mots.size > 0) {
                            addAll(mots)
                        }
                    }
                }
            })
    }

    suspend fun updateCategoriePictogramme(context: Context, id: Long) {
        val apiClient = ApiClient()
        var mots: ArrayList<Mot>
        apiClient.getApiService(context).getmotsByCategory(id)
            .enqueue(object : retrofit2.Callback<EmbeddedResponse<Mot>> {
                override fun onFailure(
                    call: Call<EmbeddedResponse<Mot>>,
                    t: Throwable
                ) {
                    // Error fetching
                    println("erreur fetching")
                    println(t.printStackTrace())
                }

                override fun onResponse(
                    call: Call<EmbeddedResponse<Mot>>,
                    response: Response<EmbeddedResponse<Mot>>
                ) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val temp = response.body()
                        mots = temp?.data as ArrayList<Mot>
                        if (mots.size > 0) {
                            addAll(mots)
                        }
                    }
                }
            })
    }

    companion object {
        private var ourInstance: MotRepository? = null
        fun getInstance(context: Context): MotRepository? {
            if (ourInstance == null) {
                ourInstance = MotRepository(context)
            }
            return ourInstance
        }
    }

    init {
        bdd = AppDatabase.getInstance(context)!!
        pictogrammes = allPictogrammes
    }
}
