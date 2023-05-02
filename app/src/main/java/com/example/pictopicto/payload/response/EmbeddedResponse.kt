package com.example.pictopicto.payload.response

import com.google.gson.annotations.SerializedName


data class EmbeddedResponse<T>(

    @SerializedName("content")
    var data: List<T>
)