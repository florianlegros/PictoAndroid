package com.example.pictopicto.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.pictopicto.api.ApiClient
import com.example.pictopicto.dao.AppDatabase
import com.example.pictopicto.model.Question
import com.example.pictopicto.model.Tag
import com.example.pictopicto.payload.response.EmbeddedResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class TagRepository private constructor(context: Context) {
    var tags: LiveData<List<Tag>>
    private val bdd: AppDatabase

    private val allTags: LiveData<List<Tag>>
        get() = bdd.tagDao()!!.getAll()

    suspend fun addAll(Tags: List<Tag>) {
        bdd.tagDao()?.insertAll(Tags)
    }

    fun getAll(): LiveData<List<Tag>>? {
        return bdd.tagDao()?.getAll()
    }

    suspend fun getTagById(TagId: Long): Tag? {
        return bdd.tagDao()?.getTagById(TagId)
    }

    suspend fun insertTag(Tag: Tag) {
        bdd.tagDao()?.insertTag(Tag)
    }

    suspend fun updateDatabase(context: Context) {
        val apiClient = ApiClient()
        var tags: ArrayList<Tag> = ArrayList()
        apiClient.getApiService(context).getTags()
            .enqueue(object : retrofit2.Callback<EmbeddedResponse<Tag>> {
                override fun onFailure(
                    call: Call<EmbeddedResponse<Tag>>,
                    t: Throwable
                ) {
                    // Error fetching
                    println("erreur fetching")
                    println(t.printStackTrace())
                }

                override fun onResponse(
                    call: Call<EmbeddedResponse<Tag>>,
                    response: Response<EmbeddedResponse<Tag>>
                ) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val temp = response.body()
                        temp?.let { tags = it as ArrayList<Tag> }
                        if (tags.size > 0) {
                            addAll(tags)
                        }
                    }
                }
            })
    }

    companion object {
        private var ourInstance: TagRepository? = null
        fun getInstance(context: Context): TagRepository? {
            if (ourInstance == null) {
                ourInstance = TagRepository(context)
            }
            return ourInstance
        }
    }

    init {
        bdd = AppDatabase.getInstance(context)!!
        tags = allTags
    }
}
