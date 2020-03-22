package com.suda.yzune.wakeupschedule.utils

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface MyRetrofitService {
    @GET("count")
    fun addCount(): Call<ResponseBody>

    @GET("get_donate")
    fun getDonateList(): Call<ResponseBody>

    @GET("getupdate")
    fun getUpdateInfo(
            @Header("version") version: Int
    ): Call<ResponseBody>

    @GET("count_html")
    fun getHtmlCount(): Call<ResponseBody>

    @HTTP(method = "POST", path = "apply_html", hasBody = true)
    @FormUrlEncoded
    fun postHtml(@Field("school") school: String,
                 @Field("type") type: String,
                 @Field("html") html: String,
                 @Field("qq") qq: String
    ): Call<ResponseBody>

    @HTTP(method = "POST", path = "share_schedule", hasBody = true)
    @FormUrlEncoded
    fun shareSchedule(
            @Header("version") version: Int,
            @Field("schedule") schedule: String
    ): Call<ResponseBody>

    @GET("share_schedule/get")
    fun getShareSchedule(
            @Header("version") version: Int,
            @Query("key") key: String): Call<ResponseBody>
}