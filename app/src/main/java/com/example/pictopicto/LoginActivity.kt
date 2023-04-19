package com.example.pictopicto

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.pictopicto.api.ApiClient
import com.example.pictopicto.databinding.ActivityLoginBinding
import com.example.pictopicto.payload.request.LoginRequest
import com.example.pictopicto.payload.response.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var apiClient: ApiClient
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiClient = ApiClient()
        sessionManager = SessionManager(this)
        val mainActivity = Intent(this, MainActivity::class.java)

        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading
        login.isEnabled = true


        login.setOnClickListener {
            loading.visibility = View.VISIBLE
            apiClient.getApiService().login(LoginRequest(username = username.text.toString(), password = password.text.toString()))
                .enqueue(object : Callback<LoginResponse> {
                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        // Error logging in
                        println("erreur")
                        println(t.printStackTrace())
                    }

                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        println("reponse")

                        if (response.code() == 200) {
                            sessionManager.saveAuthToken(response.headers()["Set-Cookie"].toString())
                            println(response.headers())
                            println(response.headers()["Set-Cookie"])
                            startActivity(mainActivity)
                        } else {
                            // Error logging in

                            println(response.raw())
                        }
                    }
                })
        }

    }

}

