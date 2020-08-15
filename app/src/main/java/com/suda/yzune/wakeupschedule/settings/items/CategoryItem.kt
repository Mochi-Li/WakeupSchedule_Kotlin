package com.suda.yzune.wakeupschedule.settings.items

import androidx.annotation.StringRes

data class CategoryItem(@StringRes val name: Int, val hasMarginTop: Boolean) : BaseSettingItem(name, null) {
    override fun getType() = SettingType.CATEGORY
}