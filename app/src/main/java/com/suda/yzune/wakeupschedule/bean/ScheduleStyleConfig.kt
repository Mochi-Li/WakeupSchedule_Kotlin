package com.suda.yzune.wakeupschedule.bean

interface ScheduleStyleConfig {
    var itemHeight: Int
    var itemAlpha: Int
    var itemTextSize: Int
    var strokeColor: Int
    var textColor: Int
    var courseTextColor: Int
    var showSat: Boolean
    var showSun: Boolean
    var sundayFirst: Boolean
    var showOtherWeekCourse: Boolean
    var showTime: Boolean
    var showTeacher: Boolean
    var showTimeBar: Boolean
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
}