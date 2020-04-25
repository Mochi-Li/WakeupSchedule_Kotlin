package com.suda.yzune.wakeupschedule.bean

import android.content.Context
import androidx.core.content.edit
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.getPrefer

open class ScheduleStyleConfig(context: Context, name: String) {

    val sp by lazy(LazyThreadSafetyMode.NONE) {
        context.getPrefer(name)
    }

    fun clear() {
        sp.edit(true) {
            clear()
        }
    }

    var itemHeight: Int = DefaultValue.itemHeight
        get() = sp.getInt("itemHeight", DefaultValue.itemHeight)
        set(value) {
            field = value
            sp.edit {
                putInt("itemHeight", value)
            }
        }

    var itemAlpha: Int = DefaultValue.itemAlpha
        get() = sp.getInt("itemAlpha", DefaultValue.itemAlpha)
        set(value) {
            field = value
            sp.edit {
                putInt("itemAlpha", value)
            }
        }

    var itemTextSize: Int = DefaultValue.itemTextSize
        get() = sp.getInt("itemTextSize", DefaultValue.itemTextSize)
        set(value) {
            field = value
            sp.edit {
                putInt("itemTextSize", value)
            }
        }

    var strokeColor: Int = DefaultValue.strokeColor
        get() = sp.getInt("strokeColor", DefaultValue.strokeColor)
        set(value) {
            field = value
            sp.edit {
                putInt("strokeColor", value)
            }
        }

    var textColor: Int = DefaultValue.textColor
        get() = sp.getInt("textColor", DefaultValue.textColor)
        set(value) {
            field = value
            sp.edit {
                putInt("textColor", value)
            }
        }

    var courseTextColor: Int = DefaultValue.courseTextColor
        get() = sp.getInt("courseTextColor", DefaultValue.courseTextColor)
        set(value) {
            field = value
            sp.edit {
                putInt("courseTextColor", value)
            }
        }

    var showSat: Boolean = DefaultValue.showSat
        get() = sp.getBoolean("showSat", DefaultValue.showSat)
        set(value) {
            field = value
            sp.edit {
                putBoolean("showSat", value)
            }
        }

    var showSun: Boolean = DefaultValue.showSun
        get() = sp.getBoolean("showSun", DefaultValue.showSun)
        set(value) {
            field = value
            sp.edit {
                putBoolean("showSun", value)
            }
        }

    var showOtherWeekCourse: Boolean = DefaultValue.showOtherWeekCourse
        get() = sp.getBoolean("showOtherWeekCourse", DefaultValue.showOtherWeekCourse)
        set(value) {
            field = value
            sp.edit {
                putBoolean("showOtherWeekCourse", value)
            }
        }

    var showTime: Boolean = DefaultValue.showTime
        get() = sp.getBoolean("showTime", DefaultValue.showTime)
        set(value) {
            field = value
            sp.edit {
                putBoolean("showTime", value)
            }
        }

    var showTeacher: Boolean = DefaultValue.showTeacher
        get() = sp.getBoolean(Const.KEY_SCHEDULE_TEACHER, DefaultValue.showTeacher)
        set(value) {
            field = value
            sp.edit {
                putBoolean(Const.KEY_SCHEDULE_TEACHER, value)
            }
        }

    var showTimeBar: Boolean = DefaultValue.showTimeBar
        get() = sp.getBoolean(Const.KEY_SCHEDULE_DETAIL_TIME, DefaultValue.showTimeBar)
        set(value) {
            field = value
            sp.edit {
                putBoolean(Const.KEY_SCHEDULE_DETAIL_TIME, value)
            }
        }

    var itemCenterHorizontal: Boolean = DefaultValue.itemCenterHorizontal
        get() = sp.getBoolean("itemCenterHorizontal", DefaultValue.itemCenterHorizontal)
        set(value) {
            field = value
            sp.edit {
                putBoolean("itemCenterHorizontal", value)
            }
        }

    var itemCenterVertical: Boolean = DefaultValue.itemCenterVertical
        get() = sp.getBoolean("itemCenterVertical", DefaultValue.itemCenterVertical)
        set(value) {
            field = value
            sp.edit {
                putBoolean("itemCenterVertical", value)
            }
        }
}

object DefaultValue {
    const val startDate: String = "2020-02-03"
    const val nodes: Int = 20
    const val maxWeek: Int = 20
    const val itemHeight: Int = 64
    const val itemAlpha: Int = 60
    const val itemTextSize: Int = 12
    const val strokeColor: Int = 0x80ffffff.toInt()
    const val textColor: Int = 0xff000000.toInt()
    const val courseTextColor: Int = 0xffffffff.toInt()
    const val showSat: Boolean = true
    const val showSun: Boolean = true
    const val sundayFirst: Boolean = false
    const val showOtherWeekCourse: Boolean = true
    const val showTime: Boolean = false
    const val showTeacher: Boolean = true
    const val showTimeBar: Boolean = true
    const val showWidgetBg: Boolean = false
    const val showDate: Boolean = true
    const val showColor: Boolean = true
    const val widgetBgColor: Int = 0x80ffffff.toInt()
    const val itemCenterHorizontal: Boolean = false
    const val itemCenterVertical: Boolean = false
}