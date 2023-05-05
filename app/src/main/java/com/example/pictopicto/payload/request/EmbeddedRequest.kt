package com.example.pictopicto.payload.request

import com.google.gson.annotations.SerializedName


data class EmbeddedRequest<T>(

    @SerializedName("content")
    var data: T
)