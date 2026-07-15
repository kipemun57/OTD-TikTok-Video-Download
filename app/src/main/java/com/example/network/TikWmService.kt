package com.example.network

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface TikWmService {
    @FormUrlEncoded
    @POST("api/")
    suspend fun analyzeVideo(
        @Field("url") url: String,
        @Field("hd") hd: Int = 1
    ): Response<TikWmResponse>
}
