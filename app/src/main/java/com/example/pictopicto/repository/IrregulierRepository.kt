package com.example.pictopicto.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.pictopicto.api.ApiClient
import com.example.pictopicto.dao.AppDatabase
import com.example.pictopicto.model.Question
import com.example.pictopicto.model.Irregulier
import com.example.pictopicto.payload.response.EmbeddedResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class IrregulierRepository private constructor(context: Context) {
    var irreguliers: LiveData<List<Irregulier>>
    private val bdd: AppDatabase

    private val allIrreguliers: LiveData<List<Irregulier>>
        get() = bdd.irregulierDao()!!.getAll()

    suspend fun addAll(Irreguliers: List<Irregulier>) {
        bdd.irregulierDao()?.insertAll(Irreguliers)
    }

    fun getAll(): LiveData<List<Irregulier>>? {
        return bdd.irregulierDao()?.getAll()
    }

    suspend fun getIrregulierById(IrregulierId: Long): Irregulier? {
        return bdd.irregulierDao()?.getIrregulierById(IrregulierId)
    }

    suspend fun insertIrregulier(Irregulier: Irregulier) {
        bdd.irregulierDao()?.insertIrregulier(Irregulier)
    }

    suspend fun updateDatabase(context: Context) {
        val apiClient = ApiClient()
        var irreguliers: ArrayList<Irregulier> = ArrayList()
        apiClient.getApiService(context).getIrreguliers()
            .enqueue(object : retrofit2.Callback<EmbeddedResponse<Irregulier>> {
                override fun onFailure(
                    call: Call<EmbeddedResponse<Irregulier>>,
                    t: Throwable
                ) {
                    // Error fetching
                    println("erreur fetching")
                    println(t.printStackTrace())
                }

                override fun onResponse(
                    call: Call<EmbeddedResponse<Irregulier>>,
                    response: Response<EmbeddedResponse<Irregulier>>
                ) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val temp = response.body()
                        temp?.let { irreguliers = it as ArrayList<Irregulier> }
                        if (irreguliers.size > 0) {
                            addAll(irreguliers)
                        }
                    }
                }
            })
    }

    companion object {
        private var ourInstance: IrregulierRepository? = null
        fun getInstance(context: Context): IrregulierRepository? {
            if (ourInstance == null) {
                ourInstance = IrregulierRepository(context)
            }
            return ourInstance
        }
    }

    init {
        bdd = AppDatabase.getInstance(context)!!
        irreguliers = allIrreguliers
    }
}
