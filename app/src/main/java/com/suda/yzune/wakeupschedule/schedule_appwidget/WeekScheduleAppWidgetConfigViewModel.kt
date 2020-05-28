package com.suda.yzune.wakeupschedule.schedule_appwidget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.suda.yzune.wakeupschedule.AppDatabase
import com.suda.yzune.wakeupschedule.bean.*

class WeekScheduleAppWidgetConfigViewModel(application: Application) : AndroidViewModel(application) {
    private val dataBase = AppDatabase.getDatabase(application)
    private val tableDao = dataBase.tableDao()
    private val widgetDao = dataBase.appWidgetDao()
    private val timeDao = dataBase.timeDetailDao()

    var widgetId = 0
    lateinit var tableConfig: TableConfig
    lateinit var widgetConfig: WidgetStyleConfig
    var timeList: MutableList<TimeDetailBean>? = null

    val daysArray by lazy(LazyThreadSafetyMode.NONE) { arrayOf("日", "一", "二", "三", "四", "五", "六", "日") }
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
        timeList = timeDao.getTimeList(1).toMutableList()
        if (timeList == null) {
            timeList = arrayListOf()
        }
        if (timeList!!.isEmpty()) {
            for (i in 0 until 30) {
                timeList!!.add(TimeDetailBean(i, "00:00", "00:00"))
            }
        }
    }

    suspend fun insertWeekAppWidgetData(appWidget: AppWidgetBean) {
        widgetDao.insertAppWidget(appWidget)
    }

    suspend fun getTableList(): List<TableBean> {
        return tableDao.getTableList()
    }

}