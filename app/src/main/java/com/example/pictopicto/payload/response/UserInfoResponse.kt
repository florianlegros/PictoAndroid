package com.example.pictopicto.payload.response

class UserInfoResponse(
    var id: Long,
    var username: String,
    var email: String,
    val roles: List<String>
)