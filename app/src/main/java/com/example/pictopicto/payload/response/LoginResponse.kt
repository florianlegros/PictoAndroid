package com.example.pictopicto.payload.response

import com.example.pictopicto.model.User
import com.google.gson.annotations.SerializedName


class LoginResponse(
    @SerializedName("status_code")
    var statusCode: Int,

    @SerializedName("auth_token")
    var authToken: String,

    @SerializedName("user")
    var user: User
)