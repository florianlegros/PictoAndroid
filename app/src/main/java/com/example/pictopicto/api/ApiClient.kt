package com.example.pictopicto.api

import android.content.Context
import com.example.pictopicto.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ApiClient {
    private lateinit var apiService: ApiService

    fun getApiService(context: Context): ApiService {

        if (!::apiService.isInitialized) {

            val retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okhttpClient(context)) // Add our Okhttp client
                .build()
            apiService = retrofit.create(ApiService::class.java)
        }
        return apiService
    }

    /**
     * Initialize OkhttpClient with our interceptor
     */
    private fun okhttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .build()
    }
}

