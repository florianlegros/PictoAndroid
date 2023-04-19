package com.example.pictopicto.payload.request

class SignupRequest {
    var username: String? = null

    var email: String? = null
    var role: Set<String>? = null

    var password: String? = null
}