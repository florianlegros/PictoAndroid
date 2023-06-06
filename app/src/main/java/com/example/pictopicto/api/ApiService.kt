package com.example.pictopicto.api

import com.example.pictopicto.Constants
import com.example.pictopicto.model.Categorie
import com.example.pictopicto.model.Phrase
import com.example.pictopicto.model.Pictogramme
import com.example.pictopicto.model.Question
import com.example.pictopicto.payload.request.EmbeddedRequest
import com.example.pictopicto.payload.request.LoginRequest
import com.example.pictopicto.payload.response.EmbeddedResponse
import com.example.pictopicto.payload.response.LoginResponse
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST(Constants.LOGIN_URL)
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("phrases")
    fun addPhrase(@Body request: EmbeddedRequest<Phrase>): Call<EmbeddedResponse<Phrase>>

    @GET("pictogrammes/all")
    fun getPictogrammes(): Call<List<Pictogramme>>

    @GET("categories/{id}/pictogrammes")
    fun getPictogrammesByCategory(@Path("id") id: Long): Call<EmbeddedResponse<Pictogramme>>

    @GET("categories/all")
    fun getCategories(): Call<List<Categorie>>

    @GET("questions")
    fun getQuestions(): Call<EmbeddedResponse<Question>>

    @GET("phrases")
    fun getPhrases(): Call<EmbeddedResponse<Phrase>>
}