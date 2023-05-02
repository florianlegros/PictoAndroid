package com.example.pictopicto.api

import com.example.pictopicto.Constants
import com.example.pictopicto.model.Categorie
import com.example.pictopicto.model.Pictogramme
import com.example.pictopicto.payload.request.LoginRequest
import com.example.pictopicto.payload.response.EmbeddedResponse
import com.example.pictopicto.payload.response.LoginResponse
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST(Constants.LOGIN_URL)
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @GET("pictogrammes")
    fun getPictogrammes(): Call<EmbeddedResponse<Pictogramme>>

    @GET("categories")
    fun getCategories(): Call<EmbeddedResponse<Categorie>>
}