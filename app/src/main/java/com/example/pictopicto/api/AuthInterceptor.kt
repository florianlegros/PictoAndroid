package com.example.pictopicto.api

import android.content.Context
import com.example.pictopicto.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {
    private val sessionManager = SessionManager(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        // If token has been saved, add it to the request
        sessionManager.fetchAuthToken()?.let {
            requestBuilder.addHeader("Cookie", it.split(";")[0])
        }

        return chain.proceed(requestBuilder.build())
    }
}