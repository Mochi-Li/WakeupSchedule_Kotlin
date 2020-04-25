package com.suda.yzune.wakeupschedule.bean

import android.content.Context
import android.graphics.Color
import androidx.core.content.edit
import com.suda.yzune.wakeupschedule.utils.ViewUtils

class TableConfig(context: Context, val id: Int)
    : ScheduleStyleConfig(context, "table${id}_config") {

    fun getTableCompat(): TableCompat {
        return TableCompat(
                id = id, tableName = tableName, nodes = nodes, startDate = startDate,
                maxWeek = maxWeek, sundayFirst = sundayFirst, itemHeight = itemHeight,
                itemAlpha = itemAlpha, itemTextSize = itemTextSize, strokeColor = strokeColor,
                textColor = textColor, courseTextColor = courseTextColor, showSat = showSat,
                showSun = showSun, showOtherWeekCourse = showOtherWeekCourse, showTime = showTime
        )
    }

    constructor(context: Context, id: Int, tableCompat: TableCompat) : this(context, id) {
        tableName = tableCompat.tableName
        nodes = tableCompat.nodes
        background = if (ViewUtils.judgeColorIsLight(tableCompat.textColor)) {
            "#${Color.GRAY}"
        } else ""
        startDate = tableCompat.startDate
        maxWeek = tableCompat.maxWeek
        sundayFirst = tableCompat.sundayFirst
        itemHeight = tableCompat.itemHeight
        itemAlpha = tableCompat.itemAlpha
        itemTextSize = tableCompat.itemTextSize
        strokeColor = tableCompat.strokeColor
        textColor = tableCompat.textColor
        courseTextColor = tableCompat.courseTextColor
        showSat = tableCompat.showSat
        showSun = tableCompat.showSun
        showOtherWeekCourse = tableCompat.showOtherWeekCourse
        showTime = tableCompat.showTime
    }

    var tableName: String = "未命名"
        get() = sp.getString("tableName", "未命名")!!
        set(value) {
            field = value
            sp.edit {
                putString("tableName", value)
            }
        }

    var nodes: Int = DefaultValue.nodes
        get() = sp.getInt("nodes", DefaultValue.nodes)
        set(value) {
            field = value
            sp.edit {
                putInt("nodes", value)
            }
        }

    var background: String = ""
        get() = sp.getString("background", "")!!
        set(value) {
            field = value
            sp.edit {
                putString("background", value)
            }
        }

    var startDate: String = DefaultValue.startDate
        get() = sp.getString("startDate", DefaultValue.startDate)!!
        set(value) {
            field = value
            sp.edit {
                putString("startDate", value)
            }
        }

    var maxWeek: Int = DefaultValue.maxWeek
        get() = sp.getInt("maxWeek", DefaultValue.maxWeek)
        set(value) {
            field = value
            sp.edit {
                putInt("maxWeek", value)
            }
        }

    var sundayFirst: Boolean = DefaultValue.sundayFirst
        get() = sp.getBoolean("sundayFirst", DefaultValue.sundayFirst)
        set(value) {
            field = value
            sp.edit {
                putBoolean("sundayFirst", value)
            }
        }

}