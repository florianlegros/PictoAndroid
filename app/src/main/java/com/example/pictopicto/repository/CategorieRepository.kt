package com.example.pictopicto.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.pictopicto.api.ApiClient
import com.example.pictopicto.dao.AppDatabase
import com.example.pictopicto.model.Categorie
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

    companion object {
        private var ourInstance: CategorieRepository? = null
        fun getInstance(context: Context): CategorieRepository? {
            if (ourInstance == null) {
                ourInstance = CategorieRepository(context)
            }
            return ourInstance
        }
    }

    suspend fun updateDatabase(context: Context) {
        val apiClient = ApiClient()
        var categories: ArrayList<Categorie> = ArrayList()
        apiClient.getApiService(context).getCategories()
            .enqueue(object : retrofit2.Callback<List<Categorie>> {
                override fun onFailure(
                    call: Call<List<Categorie>>,
                    t: Throwable
                ) {
                    println("erreur fetching")
                    println(t.printStackTrace())
                }
                override fun onResponse(
                    call: Call<List<Categorie>>,
                    response: Response<List<Categorie>>
                ) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val temp = response.body()
                        temp?.let { categories = it as ArrayList<Categorie> }
                        if (categories.size > 0) {
                            addAll(categories)
                        }
                    }
                }
            })
    }
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




    init {
        bdd = AppDatabase.getInstance(context)!!
        categories = allCategories
    }
}
