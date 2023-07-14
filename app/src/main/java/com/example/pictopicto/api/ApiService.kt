package com.example.pictopicto.api

import com.example.pictopicto.Constants
import com.example.pictopicto.model.*
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
    fun getMots(): Call<List<Mot>>

    @GET("categories/{id}/mots")
    fun getmotsByCategory(@Path("id") id: Long): Call<EmbeddedResponse<Mot>>

    @GET("categories/all")
    fun getCategories(): Call<List<Categorie>>

    @GET("tags")
    fun getTags(): Call<EmbeddedResponse<com.example.pictopicto.model.Tag>>

    @GET("irreguliers")
    fun getIrreguliers(): Call<EmbeddedResponse<Irregulier>>

    @GET("conjugaison")
    fun getConjugaisons(): Call<EmbeddedResponse<Conjugaison>>

    @GET("questions")
    fun getQuestions(): Call<EmbeddedResponse<Question>>

    @GET("phrases")
    fun getPhrases(): Call<EmbeddedResponse<Phrase>>
}