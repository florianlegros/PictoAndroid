package com.example.pictopicto.api

import com.example.pictopicto.Constants
import com.example.pictopicto.payload.request.LoginRequest
import com.example.pictopicto.payload.response.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {

    @POST(Constants.LOGIN_URL)
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}