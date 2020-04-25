package com.suda.yzune.wakeupschedule.bean

import android.content.Context
import androidx.core.content.edit
import com.suda.yzune.wakeupschedule.utils.Const

class WidgetStyleConfig(context: Context, id: Int)
    : ScheduleStyleConfig(context, "widget${id}_config") {

    var tableId: Int = 0
        get() = sp.getInt("tableId", 0)
        set(value) {
            field = value
            sp.edit {
                putInt("tableId", value)
            }
        }

    var showBg: Boolean = DefaultValue.showWidgetBg
        get() = sp.getBoolean(Const.KEY_APPWIDGET_BG, DefaultValue.showWidgetBg)
        set(value) {
            field = value
            sp.edit {
                putBoolean(Const.KEY_APPWIDGET_BG, value)
            }
        }

    var showDate: Boolean = DefaultValue.showDate
        get() = sp.getBoolean("showDate", DefaultValue.showDate)
        set(value) {
            field = value
            sp.edit {
                putBoolean("showDate", value)
            }
        }

    var showColor: Boolean = DefaultValue.showColor
        get() = sp.getBoolean("showColor", DefaultValue.showColor)
        set(value) {
            field = value
            sp.edit {
                putBoolean("showColor", value)
            }
        }

    var bgColor: Int = DefaultValue.widgetBgColor
        get() = sp.getInt(Const.KEY_APPWIDGET_BG_COLOR, DefaultValue.widgetBgColor)
        set(value) {
            field = value
            sp.edit {
                putInt(Const.KEY_APPWIDGET_BG_COLOR, value)
            }
        }

}