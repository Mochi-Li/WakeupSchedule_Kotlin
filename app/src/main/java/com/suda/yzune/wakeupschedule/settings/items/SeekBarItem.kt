package com.suda.yzune.wakeupschedule.settings.items

import androidx.annotation.StringRes

data class SeekBarItem(
        @StringRes val name: Int,
        var valueInt: Int,
        val min: Int,
        var max: Int,
        val unit: String,
        val prefix: String = "",
        val keys: List<String>? = null) : BaseSettingItem(name, keys) {
    override fun getType(): Int {
        return SettingType.SEEKBAR
    }
}