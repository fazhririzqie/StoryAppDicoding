package com.example.storyapp.data

import com.google.gson.annotations.SerializedName

data class AddStoryResponse(


    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("error")
    val error: Boolean
)
