package com.suda.yzune.wakeupschedule.settings.items

import androidx.annotation.StringRes

data class SwitchItem(
        @StringRes val name: Int,
        var checked: Boolean,
        var desc: String = "",
        val keys: List<String>? = null) : BaseSettingItem(name, keys) {
    override fun getType(): Int {
        return SettingType.SWITCH
    }
}