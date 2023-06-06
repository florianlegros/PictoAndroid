package com.example.pictopicto.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.pictopicto.api.ApiClient
import com.example.pictopicto.dao.AppDatabase
import com.example.pictopicto.model.Pictogramme
import com.example.pictopicto.payload.response.EmbeddedResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class PictogrammeRepository private constructor(context: Context) {
    var pictogrammes: LiveData<List<Pictogramme>>
    var pictogrammesByCategorie: MutableLiveData<List<Pictogramme>> = MutableLiveData()
    private val bdd: AppDatabase
    var categorieId: Long = 17

    private val allPictogrammes: LiveData<List<Pictogramme>>
        get() = bdd.pictogrammeDao()!!.getAll()

    suspend fun addAll(pictogrammes: List<Pictogramme>) {
        bdd.pictogrammeDao()?.insertAll(pictogrammes)
    }

    fun getAll() {
        bdd.pictogrammeDao()?.getAll()
    }

    suspend fun getPictogrammeById(pictogrammeId: Long): Pictogramme? {
        return bdd.pictogrammeDao()?.getPictogrammeById(pictogrammeId)
    }

    suspend fun getAllPictogrammeByCategorieId(categorieId: Long): List<Pictogramme>? {
        val temp = bdd.pictogrammeDao()?.getAllPictogrammeByCategorieId(categorieId)
        pictogrammesByCategorie.postValue(temp)
        return temp
    }

    suspend fun insertPictogramme(pictogramme: Pictogramme) {
        bdd.pictogrammeDao()?.insertPictogramme(pictogramme)
    }

    suspend fun updateDatabase(context: Context) {
        val apiClient = ApiClient()
        var pictogrammes: ArrayList<Pictogramme> = ArrayList()
        apiClient.getApiService(context).getPictogrammes()
            .enqueue(object : retrofit2.Callback<List<Pictogramme>> {
                override fun onFailure(
                    call: Call<List<Pictogramme>>,
                    t: Throwable
                ) {
                    // Error fetching
                    println("erreur fetching")
                    println(t.printStackTrace())
                }

                override fun onResponse(
                    call: Call<List<Pictogramme>>,
                    response: Response<List<Pictogramme>>
                ) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val temp = response.body()
                        temp?.let { pictogrammes = it as ArrayList<Pictogramme> }
                        if (pictogrammes.size > 0) {
                            addAll(pictogrammes)
                        }
                    }
                }
            })
    }

    suspend fun updateCategoriePictogramme(context: Context, id: Long) {
        val apiClient = ApiClient()
        var pictogrammes: ArrayList<Pictogramme>
        apiClient.getApiService(context).getPictogrammesByCategory(id)
            .enqueue(object : retrofit2.Callback<EmbeddedResponse<Pictogramme>> {
                override fun onFailure(
                    call: Call<EmbeddedResponse<Pictogramme>>,
                    t: Throwable
                ) {
                    // Error fetching
                    println("erreur fetching")
                    println(t.printStackTrace())
                }

                override fun onResponse(
                    call: Call<EmbeddedResponse<Pictogramme>>,
                    response: Response<EmbeddedResponse<Pictogramme>>
                ) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val temp = response.body()
                        pictogrammes = temp?.data as ArrayList<Pictogramme>
                        if (pictogrammes.size > 0) {
                            addAll(pictogrammes)
                        }
                    }
                }
            })
    }

    companion object {
        private var ourInstance: PictogrammeRepository? = null
        fun getInstance(context: Context): PictogrammeRepository? {
            if (ourInstance == null) {
                ourInstance = PictogrammeRepository(context)
            }
            return ourInstance
        }
    }

    init {
        bdd = AppDatabase.getInstance(context)!!
        pictogrammes = allPictogrammes
    }
}
