package com.suda.yzune.wakeupschedule.bean

data class MyResponse<T>(
        val `data`: T,
        val message: String,
        val status: String
)

data class UpdateInfo(
        val donate: Boolean,
        val id: Int,
        val versionInfo: String,
        val versionName: String
)