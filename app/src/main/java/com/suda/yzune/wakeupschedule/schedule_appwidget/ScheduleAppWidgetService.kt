package com.suda.yzune.wakeupschedule.schedule_appwidget

import android.content.Intent
import android.widget.LinearLayout
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import android.widget.TextView
import com.suda.yzune.wakeupschedule.AppDatabase
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.bean.*
import com.suda.yzune.wakeupschedule.schedule.ScheduleUI
import com.suda.yzune.wakeupschedule.utils.CourseUtils
import com.suda.yzune.wakeupschedule.utils.CourseUtils.countWeek
import com.suda.yzune.wakeupschedule.utils.ViewUtils
import java.text.ParseException

class ScheduleAppWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        if (intent != null) {
            val list = intent.data?.schemeSpecificPart?.split(",")
                    ?: return ScheduleRemoteViewsFactory()
            if (list.size < 2) {
                return ScheduleRemoteViewsFactory(nextWeek = (list[0] == "1"))
            }
            return ScheduleRemoteViewsFactory(list[1].toInt(), list[0] == "1")
        } else {
            return ScheduleRemoteViewsFactory()
        }
    }

    private inner class ScheduleRemoteViewsFactory(val appWidgetId: Int = -1, val nextWeek: Boolean = false) : RemoteViewsFactory {
        private lateinit var table: TableBean
        private lateinit var tableConfig: TableConfig
        private lateinit var scheduleConfig: WidgetStyleConfig
        private var week = 0
        private val dataBase = AppDatabase.getDatabase(applicationContext)
        private val tableDao = dataBase.tableDao()
        private val courseDao = dataBase.courseDao()
        private val timeDao = dataBase.timeDetailDao()
        private val timeList = arrayListOf<TimeDetailBean>()
        private val weekDay = CourseUtils.getWeekdayInt()
        private val allCourseList = Array(7) { listOf<CourseBean>() }
        private var isTableDelete = false

        override fun onCreate() {}

        override fun onDataSetChanged() {
            if (appWidgetId == -1) return
            scheduleConfig = WidgetStyleConfig(applicationContext, appWidgetId)
            tableConfig = TableConfig(applicationContext, scheduleConfig.tableId)

            try {
                week = if (nextWeek) countWeek(tableConfig.startDate, tableConfig.sundayFirst) + 1
                else countWeek(tableConfig.startDate, tableConfig.sundayFirst)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            if (week <= 0) {
                week = 1
            }

            tableDao.getTableByIdSync(scheduleConfig.tableId).let {
                if (it != null) {
                    table = it
                } else {
                    isTableDelete = true
                    return
                }
            }

            for (i in 1..7) {
                allCourseList[i - 1] = courseDao.getCourseByDayOfTableSync(i, table.id)
            }

            timeList.clear()
            timeList.addAll(timeDao.getTimeListSync(table.timeTable))
        }

        override fun onDestroy() {
            timeList.clear()
        }

        override fun getCount(): Int {
            return 1
        }

        override fun getViewAt(position: Int): RemoteViews {
            if (isTableDelete) {
                val rv = RemoteViews(applicationContext.packageName, R.layout.appwidget_loading_view)
                rv.setTextViewText(R.id.tv_tips, "选择显示的课表已被删除\n请移除或重新设置此小部件")
                return rv
            }
            val mRemoteViews = RemoteViews(applicationContext.packageName, R.layout.item_schedule_widget)
            if (position < 0 || position >= 1) return mRemoteViews
            initData(mRemoteViews)
            return mRemoteViews
        }

        override fun getLoadingView(): RemoteViews? {
            return RemoteViews(applicationContext.packageName, R.layout.appwidget_loading_view)
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun hasStableIds(): Boolean {
            return false
        }

        fun initData(views: RemoteViews) {
            val ui = ScheduleUI(applicationContext, tableConfig, scheduleConfig, weekDay, true)
            if (timeList.isNotEmpty() && ui.showTimeDetail) {
                for (i in 0 until tableConfig.nodes) {
                    (ui.content.getViewById(R.id.anko_tv_node1 + i) as LinearLayout).apply {
                        findViewById<TextView>(R.id.tv_start).text = timeList[i].startTime
                        findViewById<TextView>(R.id.tv_end).text = timeList[i].endTime
                    }
                }
            }
            for (i in 1..7) {
                ui.initWeekPanel(allCourseList[i - 1], timeList, week, i)
            }
            val scrollView = ui.scrollView
            val info = ViewUtils.getScreenInfo(applicationContext)
            if (info[0] < info[1]) {
                ViewUtils.layoutView(scrollView, info[0], info[1])
            } else {
                ViewUtils.layoutView(scrollView, info[1], info[0])
            }
            views.setBitmap(R.id.iv_schedule, "setImageBitmap", ViewUtils.getViewBitmap(scrollView))
            scrollView.removeAllViews()
        }

    }

}