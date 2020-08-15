package com.suda.yzune.wakeupschedule.settings.items

import androidx.annotation.StringRes

data class HorizontalItem(
        @StringRes val name: Int,
        var value: String = "",
        val keys: List<String>? = null) : BaseSettingItem(name, keys) {
    override fun getType(): Int {
        return SettingType.HORIZON
    }
}