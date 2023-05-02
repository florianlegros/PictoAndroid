package com.example.pictopicto.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.pictopicto.api.ApiClient
import com.example.pictopicto.dao.AppDatabase
import com.example.pictopicto.model.Categorie
import com.example.pictopicto.payload.response.EmbeddedResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class CategorieRepository private constructor(context: Context) {
    var categories: LiveData<List<Categorie>>
    private val bdd: AppDatabase

    private val allCategories: LiveData<List<Categorie>>
        get() = bdd.categorieDao()!!.getAll()

    suspend fun addAll(Categories: List<Categorie>) {
        bdd.categorieDao()?.insertAll(Categories)
    }
    fun getAll(): LiveData<List<Categorie>>? {
       return bdd.categorieDao()?.getAll()
    }
    suspend fun getCategorieById(CategorieId: Long): Categorie? {
        return bdd.categorieDao()?.getCategorieById(CategorieId)
    }

    suspend fun insertCategorie(Categorie: Categorie) {
        bdd.categorieDao()?.insertCategorie(Categorie)
    }

    suspend fun updateDatabase(context: Context) {
        val apiClient = ApiClient()
        var Categories: ArrayList<Categorie>
        apiClient.getApiService(context).getCategories()
            .enqueue(object : retrofit2.Callback<EmbeddedResponse<Categorie>> {
                override fun onFailure(
                    call: Call<EmbeddedResponse<Categorie>>,
                    t: Throwable
                ) {
                    // Error fetching
                    println("erreur fetching")
                    println(t.printStackTrace())
                }

                override fun onResponse(
                    call: Call<EmbeddedResponse<Categorie>>,
                    response: Response<EmbeddedResponse<Categorie>>
                ) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val temp = response.body()
                        Categories = temp?.data as ArrayList<Categorie>
                        if (Categories.size > 0) {
                            addAll(Categories)
                        }
                    }
                }
            })
    }

    companion object {
        private var ourInstance: CategorieRepository? = null
        fun getInstance(context: Context): CategorieRepository? {
            if (ourInstance == null) {
                ourInstance = CategorieRepository(context)
            }
            return ourInstance
        }
    }

    init {
        bdd = AppDatabase.getInstance(context)!!
        categories = allCategories
    }
}
