package com.example.pictopicto.api

import com.example.pictopicto.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient {
    private lateinit var apiService: ApiService


    fun getApiService(): ApiService {
        if(!::apiService.isInitialized){
            val retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            println(retrofit.baseUrl())
            apiService = retrofit.create(ApiService::class.java)
        }
        return apiService
    }
}