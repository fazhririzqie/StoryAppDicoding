package com.example.storyapp.api

import com.example.storyapp.data.AddStoryResponse
import com.example.storyapp.data.SignInResponse
import com.example.storyapp.data.SignUpResponse
import com.example.storyapp.data.StoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @GET("v1/stories")
    fun getListStory(
        @Header("Authorization") bearer: String?
    ): Call<StoryResponse>

    @Multipart
    @POST("/v1/stories")
    fun postStory(
        @Header("Authorization") bearer: String?,
        @Part photo: MultipartBody.Part,
        @Part("description") description: RequestBody?,
        @Part("lat") lat: RequestBody?,
        @Part("lon") lon: RequestBody?
    ): Call<AddStoryResponse>

    @FormUrlEncoded
    @POST("/v1/register")
    fun doSignup(
        @Field("name") name: String?,
        @Field("email") email: String?,
        @Field("password") password: String?
    ): Call<SignUpResponse>

    @GET("v1/stories")
    suspend fun getStoriesWithLocation(
        @Header("Authorization") bearer: String?,
        @Query("location") location: Int = 1
    ): StoryResponse

    @FormUrlEncoded
    @POST("/v1/login")
    fun doSignin(
        @Field("email") email: String?,
        @Field("password") password: String?
    ): Call<SignInResponse>

        @GET("v1/stories")
        suspend fun getStories(
            @Header("Authorization") bearer: String?,
            @Query("page") page: Int = 1,
            @Query("size") size: Int = 20
        ): StoryResponse

}