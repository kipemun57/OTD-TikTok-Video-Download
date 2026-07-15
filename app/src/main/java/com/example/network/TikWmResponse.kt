package com.example.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TikWmResponse(
    val code: Int,
    val msg: String?,
    val data: TikWmVideoData?
)

@JsonClass(generateAdapter = true)
data class TikWmVideoData(
    val id: String?,
    val title: String?,
    val cover: String?,
    val origin_cover: String?,
    val duration: Int?,
    val play: String?,
    val wmplay: String?,
    val hdplay: String?,
    val size: Long?,
    val hd_size: Long?,
    val wm_size: Long?,
    val music: String?,
    val music_info: TikWmMusicInfo?,
    val author: TikWmAuthor?
)

@JsonClass(generateAdapter = true)
data class TikWmMusicInfo(
    val title: String?,
    val play: String?,
    val author: String?
)

@JsonClass(generateAdapter = true)
data class TikWmAuthor(
    val id: String?,
    val unique_id: String?,
    val nickname: String?,
    val avatar: String?
)
