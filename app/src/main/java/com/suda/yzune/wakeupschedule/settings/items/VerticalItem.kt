package com.suda.yzune.wakeupschedule.settings.items

import androidx.annotation.StringRes

data class VerticalItem(
        @StringRes val name: Int,
        val description: String = "",
        val isSpanned: Boolean = false,
        val keys: List<String>? = null) : BaseSettingItem(name, keys) {
    override fun getType(): Int {
        return SettingType.VERTICAL
    }
}