package com.example.pictopicto.model

import com.google.gson.annotations.SerializedName

class User(
    @SerializedName("id")
    var id: Long,

    @SerializedName("username")
    var username: String,

    @SerializedName("email")
    var email: String,

    @SerializedName("password")
    var password: String,
)