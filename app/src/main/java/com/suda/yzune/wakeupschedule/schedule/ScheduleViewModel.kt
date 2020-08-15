package com.suda.yzune.wakeupschedule.schedule

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import biweekly.Biweekly
import biweekly.ICalVersion
import biweekly.ICalendar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.suda.yzune.wakeupschedule.App
import com.suda.yzune.wakeupschedule.AppDatabase
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.bean.*
import com.suda.yzune.wakeupschedule.schedule_import.Common
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.CourseUtils
import com.suda.yzune.wakeupschedule.utils.ICalUtils
import com.suda.yzune.wakeupschedule.utils.getPrefer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val dataBase = AppDatabase.getDatabase(application)
    private val courseDao = dataBase.courseDao()
    private val tableDao = dataBase.tableDao()
    private val widgetDao = dataBase.appWidgetDao()
    private val timeTableDao = dataBase.timeTableDao()
    private val timeDao = dataBase.timeDetailDao()

    lateinit var table: TableBean
    lateinit var tableConfig: TableConfig
    lateinit var timeList: List<TimeDetailBean>
    var selectedWeek = 1
    val marTop = application.resources.getDimensionPixelSize(R.dimen.weekItemMarTop)
    var itemHeight = 0
    var alphaInt = 225
    val allCourseList = Array(7) { MutableLiveData<List<CourseBean>>() }
    val daysArray by lazy(LazyThreadSafetyMode.NONE) {
        application.resources.getStringArray(R.array.main_weekdays)
    }
    var currentWeek = 1

    suspend fun initTableSelectList(): MutableList<TableConfig> {
        return tableDao.getTableList().map {
            TableConfig(getApplication(), it.id)
        }.toMutableList()
    }

    fun getMultiCourse(week: Int, day: Int, startNode: Int): List<CourseBean> {
        return allCourseList[day - 1].value!!.filter {
            it.inWeek(week) && it.startNode == startNode
        }
    }

    suspend fun getTableById(id: Int): TableBean {
        return tableDao.getTableById(id) ?: TableBean(id)
    }

    suspend fun getTimeList(timeTableId: Int): List<TimeDetailBean> {
        return timeDao.getTimeList(timeTableId)
    }

    suspend fun addBlankTable(tableName: String): TableConfig {
        val id = tableDao.insertTable(TableBean(id = 0))
        return TableConfig(getApplication(), id.toInt()).apply {
            this.tableName = tableName
            this.startDate = DefaultValue.startDate
        }
    }

    fun changeDefaultTable(id: Int) {
        getApplication<App>().getPrefer().edit {
            putInt(Const.KEY_SHOW_TABLE_ID, id)
        }
    }

    suspend fun getScheduleWidgetIds(): List<AppWidgetBean> {
        return widgetDao.getWidgetsByBaseType(0)
    }

    fun getRawCourseByDay(day: Int, tableId: Int): LiveData<List<CourseBean>> {
        return courseDao.getCourseByDayOfTableLiveData(day, tableId)
    }

    fun getShowCourseNumber(week: Int): LiveData<Int> {
        return if (tableConfig.showOtherWeekCourse) {
            courseDao.getShowCourseNumberWithOtherWeek(table.id, week)
        } else {
            courseDao.getShowCourseNumber(table.id, week)
        }
    }

    suspend fun deleteCourseBean(courseBean: CourseBean) {
        courseDao.deleteCourseDetail(CourseUtils.courseBean2DetailBean(courseBean))
    }

    suspend fun deleteCourseDetailThisWeek(courseBean: CourseBean, week: Int) {
        courseDao.deleteCourseDetailThisWeek(CourseUtils.courseBean2DetailBean(courseBean), week)
    }

    suspend fun deleteCourseDetailOfDayAllWeek(c: CourseBean) {
        courseDao.deleteCourseDetailOfDayAllWeek(c.tableId, c.id, c.day, c.startNode, c.step,
                c.room ?: "", c.teacher ?: "")
    }

    suspend fun deleteCourseBaseBean(id: Int, tableId: Int) {
        courseDao.deleteCourseBaseBeanOfTable(id, tableId)
    }

    suspend fun updateFromOldVer(json: String) {
        val gson = Gson()
        val list = gson.fromJson<List<CourseOldBean>>(json, object : TypeToken<List<CourseOldBean>>() {
        }.type)
        val lastId = tableDao.getLastId()
        val tableId = if (lastId != null) {
            lastId + 1
        } else {
            1
        }
        oldBean2CourseBean(list, tableId)
    }

    private suspend fun oldBean2CourseBean(list: List<CourseOldBean>, tableId: Int) {
        val baseList = arrayListOf<CourseBaseBean>()
        val detailList = arrayListOf<CourseDetailBean>()
        var id = 0
        for (oldBean in list) {
            val flag = Common.findExistedCourseId(baseList, oldBean.name)
            if (flag == -1) {
                baseList.add(CourseBaseBean(id, oldBean.name, "", tableId))
                detailList.add(CourseDetailBean(
                        id = id, room = oldBean.room,
                        teacher = oldBean.teach, day = oldBean.day,
                        step = oldBean.step, startWeek = oldBean.startWeek, endWeek = oldBean.endWeek,
                        type = oldBean.isOdd, startNode = oldBean.start,
                        tableId = tableId
                ))
                id++
            } else {
                detailList.add(CourseDetailBean(
                        id = flag, room = oldBean.room,
                        teacher = oldBean.teach, day = oldBean.day,
                        step = oldBean.step, startWeek = oldBean.startWeek, endWeek = oldBean.endWeek,
                        type = oldBean.isOdd, startNode = oldBean.start,
                        tableId = tableId
                ))
            }
        }
        courseDao.insertCourses(baseList, detailList)
        getApplication<App>().getPrefer().edit {
            remove(Const.KEY_OLD_VERSION_COURSE)
        }
    }

    suspend fun exportData(): String {
        val gson = Gson()
        val strBuilder = StringBuilder()
        strBuilder.append(gson.toJson(timeTableDao.getTimeTable(table.timeTable)))
        strBuilder.append("\n${gson.toJson(timeList)}")
        strBuilder.append("\n${gson.toJson(tableConfig.getTableCompat())}")
        strBuilder.append("\n${gson.toJson(courseDao.getCourseBaseBeanOfTable(table.id))}")
        strBuilder.append("\n${gson.toJson(courseDao.getDetailOfTable(table.id))}")
        return strBuilder.toString()
    }

    suspend fun exportData(uri: Uri?) {
        if (uri == null) throw Exception("无法获取文件")
        try {
            withContext(Dispatchers.IO) {
                val outputStream = getApplication<App>().contentResolver.openOutputStream(uri)
                outputStream?.write(exportData().toByteArray())
            }
        } catch (e: Exception) {
            throw Exception("请选择其他「具体的」位置，不要在「下载」或「文档」等文件夹导出")
        }
    }

    suspend fun exportICS(uri: Uri?) {
        if (uri == null) throw Exception("无法获取文件")
        val ical = ICalendar()
        withContext(Dispatchers.Default) {
            ical.setProductId("-//YZune//WakeUpSchedule//EN")
            val startTimeMap = ICalUtils.getClassTime(timeList, true)
            val endTimeMap = ICalUtils.getClassTime(timeList, false)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
            val date = sdf.parse(tableConfig.startDate)
            val cal = Calendar.getInstance()
            cal.time = date
            allCourseList.forEach {
                it.value?.forEach { course ->
                    try {
                        ICalUtils.getClassEvents(ical, startTimeMap, endTimeMap, tableConfig.maxWeek, course, cal)
                    } catch (ignored: Exception) {

                    }
                }
            }
        }
        val warnings = ical.validate(ICalVersion.V2_0)
        Log.d("日历", warnings.toString())
        try {
            withContext(Dispatchers.IO) {
                val outputStream = getApplication<App>().contentResolver.openOutputStream(uri)
                Biweekly.write(ical).go(outputStream)
            }
        } catch (e: Exception) {
            throw Exception("请选择其他「具体的」位置，不要在「下载」或「文档」等文件夹导出")
        }
    }
}