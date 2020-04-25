package com.suda.yzune.wakeupschedule.course_add

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.suda.yzune.wakeupschedule.AppDatabase
import com.suda.yzune.wakeupschedule.bean.AppWidgetBean
import com.suda.yzune.wakeupschedule.bean.CourseBaseBean
import com.suda.yzune.wakeupschedule.bean.CourseDetailBean
import com.suda.yzune.wakeupschedule.bean.CourseEditBean
import com.suda.yzune.wakeupschedule.utils.CourseUtils
import com.suda.yzune.wakeupschedule.utils.ViewUtils

class AddCourseViewModel(application: Application) : AndroidViewModel(application) {

    val editList = mutableListOf<CourseEditBean>()
    val baseBean: CourseBaseBean by lazy(LazyThreadSafetyMode.NONE) {
        CourseBaseBean(-1, "", "", tableId)
    }

    var teacherList: ArrayList<String>? = null
    var roomList: ArrayList<String>? = null

    private val dataBase = AppDatabase.getDatabase(application)
    private val courseDao = dataBase.courseDao()
    private val widgetDao = dataBase.appWidgetDao()
    private val saveList = mutableListOf<CourseDetailBean>()

    var updateFlag = true
    var newId = -1
    var tableId = 0
    var maxWeek = 30
    var nodes = 11

    fun judgeType(list: ArrayList<Int>): Int {
        val oddListCount = list.count {
            it % 2 == 1
        }
        val evenCount = maxWeek / 2
        val oddCount = maxWeek - evenCount
        // 0表示不是全部的单周也不是全部的双周
        if (oddCount == oddListCount && oddCount == list.size) {
            return 1
        }
        if (evenCount == list.size && oddListCount == 0) {
            return 2
        }
        return 0
    }

    suspend fun preSaveData(isSame: Boolean = false) {
        saveList.clear()
        if (baseBean.id == -1) {
            updateFlag = false
            baseBean.id = newId
            editList.forEach {
                it.id = newId
            }
        } else {
            editList.forEach {
                it.id = baseBean.id
            }
        }
        if (baseBean.color == "") {
            baseBean.color = "#${Integer.toHexString(ViewUtils.getCustomizedColor(getApplication(), baseBean.id % 9))}"
        }
        for (i in editList.indices) {
            saveList.addAll(CourseUtils.editBean2DetailBeanList(editList[i]))
        }

        val selfUnique = CourseUtils.checkSelfUnique(saveList)

        if (selfUnique) {
            saveData(isSame)
        } else {
            throw Exception("此处填写的时间有重复，请仔细检查")
        }
    }

    private suspend fun saveData(isSame: Boolean = false) {
        if (isSame) {
            courseDao.updateSameCourse(baseBean, saveList)
            return
        }
        if (updateFlag) {
            courseDao.updateSingleCourse(baseBean, saveList)
        } else {
            courseDao.insertSingleCourse(baseBean, saveList)
        }
    }

    suspend fun checkSameName(): CourseBaseBean? {
        return courseDao.checkSameNameInTable(baseBean.courseName, baseBean.tableId)
    }

    fun initData(maxWeek: Int): MutableList<CourseEditBean> {
        if (editList.isNotEmpty()) return editList
        editList.add(CourseEditBean(
                tableId = tableId,
                weekList = MutableLiveData<ArrayList<Int>>().apply {
                    this.value = (1..maxWeek).toCollection(ArrayList())
                }))
        return editList
    }

    suspend fun initData(id: Int, tableId: Int) {
        val detailList = courseDao.getDetailByIdOfTable(id, tableId)
        if (detailList.isEmpty()) {
            initData(maxWeek)
            return
        }
        editList.add(CourseUtils.detailBean2EditBean(detailList[0]))
        for (i in (1 until detailList.size)) {
            if (judgeMergeCourse(detailList[i], detailList[i - 1])) {
                editList.last().weekList.value?.apply {
                    addAll(getWeekIntList(detailList[i]))
                    sort()
                }
            } else {
                editList.add(CourseUtils.detailBean2EditBean(detailList[i]))
            }
        }
    }

    private fun getWeekIntList(c: CourseDetailBean): List<Int> {
        return when (c.type) {
            0 -> {
                (c.startWeek..c.endWeek).toList()
            }
            else -> {
                (c.startWeek..c.endWeek step 2).toList()
            }
        }
    }

    private fun judgeMergeCourse(a: CourseDetailBean, b: CourseDetailBean): Boolean {
        return a.day == b.day && a.startNode == b.startNode && a.step == b.step
                && a.teacher == b.teacher && a.room == b.room
    }

    suspend fun getLastId(): Int? {
        return courseDao.getLastIdOfTable(tableId)
    }

    suspend fun initBaseData(id: Int): CourseBaseBean {
        return courseDao.getCourseByIdOfTable(id, tableId)
    }

    suspend fun getScheduleWidgetIds(): List<AppWidgetBean> {
        return widgetDao.getWidgetsByBaseType(0)
    }

    suspend fun getExistedTeachers(): ArrayList<String> {
        return ArrayList(courseDao.getExistedTeachers(tableId))
    }

    suspend fun getExistedRooms(): ArrayList<String> {
        return ArrayList(courseDao.getExistedRooms(tableId))
    }
}