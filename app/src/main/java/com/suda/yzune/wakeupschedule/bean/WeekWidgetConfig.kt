package com.suda.yzune.wakeupschedule.bean

import android.content.Context
import androidx.core.content.edit
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.getPrefer

class WeekWidgetConfig(context: Context, id: Int) : ScheduleStyleConfig {

    private val sp by lazy(LazyThreadSafetyMode.NONE) {
        context.getPrefer("widget${id}_config")
    }

    //----------------------------------------//

    override var itemHeight: Int = DefaultValue.itemHeight
        get() = sp.getInt("itemHeight", DefaultValue.itemHeight)
        set(value) {
            field = value
            sp.edit {
                putInt("itemHeight", value)
            }
        }

    override var itemAlpha: Int = DefaultValue.itemAlpha
        get() = sp.getInt("itemAlpha", DefaultValue.itemAlpha)
        set(value) {
            field = value
            sp.edit {
                putInt("itemAlpha", value)
            }
        }

    override var itemTextSize: Int = DefaultValue.itemTextSize
        get() = sp.getInt("itemTextSize", DefaultValue.itemTextSize)
        set(value) {
            field = value
            sp.edit {
                putInt("itemTextSize", value)
            }
        }

    override var strokeColor: Int = DefaultValue.strokeColor
        get() = sp.getInt("strokeColor", DefaultValue.strokeColor)
        set(value) {
            field = value
            sp.edit {
                putInt("strokeColor", value)
            }
        }

    override var textColor: Int = DefaultValue.textColor
        get() = sp.getInt("textColor", DefaultValue.textColor)
        set(value) {
            field = value
            sp.edit {
                putInt("textColor", value)
            }
        }

    override var courseTextColor: Int = DefaultValue.courseTextColor
        get() = sp.getInt("courseTextColor", DefaultValue.courseTextColor)
        set(value) {
            field = value
            sp.edit {
                putInt("courseTextColor", value)
            }
        }

    override var showSat: Boolean = DefaultValue.showSat
        get() = sp.getBoolean("showSat", DefaultValue.showSat)
        set(value) {
            field = value
            sp.edit {
                putBoolean("showSat", value)
            }
        }

    override var showSun: Boolean = DefaultValue.showSun
        get() = sp.getBoolean("showSun", DefaultValue.showSat)
        set(value) {
            field = value
            sp.edit {
                putBoolean("showSun", value)
            }
        }

    override var sundayFirst: Boolean = DefaultValue.sundayFirst
        get() = sp.getBoolean("sundayFirst", DefaultValue.showSat)
        set(value) {
            field = value
            sp.edit {
                putBoolean("sundayFirst", value)
            }
        }

    override var showOtherWeekCourse: Boolean = DefaultValue.showOtherWeekCourse
        get() = sp.getBoolean("showOtherWeekCourse", DefaultValue.showSat)
        set(value) {
            field = value
            sp.edit {
                putBoolean("showOtherWeekCourse", value)
            }
        }

    override var showTime: Boolean = DefaultValue.showTime
        get() = sp.getBoolean("showTime", DefaultValue.showSat)
        set(value) {
            field = value
            sp.edit {
                putBoolean("showTime", value)
            }
        }

    override var showTeacher: Boolean = DefaultValue.showTeacher
        get() = sp.getBoolean(Const.KEY_SCHEDULE_TEACHER, DefaultValue.showTeacher)
        set(value) {
            field = value
            sp.edit {
                putBoolean(Const.KEY_SCHEDULE_TEACHER, value)
            }
        }

    override var showTimeBar: Boolean = DefaultValue.showTimeBar
        get() = sp.getBoolean(Const.KEY_SCHEDULE_DETAIL_TIME, DefaultValue.showTimeBar)
        set(value) {
            field = value
            sp.edit {
                putBoolean(Const.KEY_SCHEDULE_DETAIL_TIME, value)
            }
        }

}