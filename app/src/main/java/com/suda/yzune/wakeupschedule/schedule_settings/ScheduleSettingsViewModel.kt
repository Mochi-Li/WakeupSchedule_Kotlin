package com.suda.yzune.wakeupschedule.schedule_settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.suda.yzune.wakeupschedule.AppDatabase
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.bean.*
import com.suda.yzune.wakeupschedule.utils.CourseUtils
import java.util.*

class ScheduleSettingsViewModel(application: Application) : AndroidViewModel(application) {

    var mYear = 2018
    var mMonth = 9
    var mDay = 20
    lateinit var table: TableBean
    lateinit var tableConfig: TableConfig
    lateinit var termStartList: List<String>
    var timeList: List<TimeDetailBean>? = null

    private val dataBase = AppDatabase.getDatabase(application)
    private val timeDao = dataBase.timeDetailDao()
    private val widgetDao = dataBase.appWidgetDao()

    val daysArray by lazy(LazyThreadSafetyMode.NONE) {
        application.resources.getStringArray(R.array.main_weekdays)
    }
    val courseArray by lazy(LazyThreadSafetyMode.NONE) {
        arrayOf(listOf(CourseBean(0, "高等数学", 1,
                "理工楼110", "小洁", 1, 2, 1, 20, 0, "#2979ff", tableConfig.id)),
                listOf(CourseBean(1, "大学英语", 2,
                        "逸夫楼201", "Louis", 2, 2, 2, 20, 2, "#2979ff", tableConfig.id)),
                listOf(CourseBean(0, "计算机基础", 3,
                        "文成楼125", "老陈", 1, 3, 1, 17, 1, "#ff9100", tableConfig.id)),
                listOf(CourseBean(0, "线性代数", 4,
                        "东教楼502", "小邹", 2, 2, 1, 17, 1, "#ff3d00", tableConfig.id)),
                listOf(),
                listOf(),
                listOf(CourseBean(0, "理论力学", 7,
                        "文思楼202", "小刘", 1, 2, 1, 20, 0, "#1de9b6", tableConfig.id))
        )
    }

    suspend fun initTimeList() {
        timeList = timeDao.getTimeList(table.timeTable)
    }

    suspend fun getScheduleWidgetIds(): List<AppWidgetBean> {
        return widgetDao.getWidgetsByBaseType(0)
    }

    fun getCurrentWeek(): Int {
        return CourseUtils.countWeek(tableConfig.startDate, tableConfig.sundayFirst)
    }

    fun setCurrentWeek(week: Int) {
        val cal = Calendar.getInstance()
        if (tableConfig.sundayFirst) {
            cal.firstDayOfWeek = Calendar.SUNDAY
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        } else {
            cal.firstDayOfWeek = Calendar.MONDAY
            val d = cal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY
            if (d >= 0) {
                cal.add(Calendar.DATE, -d)
            } else {
                cal.add(Calendar.DATE, -6)
            }
        }
        cal.add(Calendar.WEEK_OF_YEAR, -week + 1)
        mYear = cal.get(Calendar.YEAR)
        mMonth = cal.get(Calendar.MONTH) + 1
        mDay = cal.get(Calendar.DATE)
        tableConfig.startDate = "${mYear}-${mMonth}-${mDay}"
    }

}