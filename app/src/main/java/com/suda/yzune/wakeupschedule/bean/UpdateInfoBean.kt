package com.suda.yzune.wakeupschedule.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UpdateInfoBean(
        val `data`: Data,
        val message: String,
        val status: String
) : Parcelable {
    @Parcelize
    data class Data(
            val donate: Boolean,
            val id: Int,
            val versionInfo: String,
            val versionName: String
    ) : Parcelable
}