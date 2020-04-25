package com.suda.yzune.wakeupschedule.today_appwidget

import android.content.Intent
import android.graphics.Bitmap
import android.view.Gravity
import android.widget.*
import androidx.core.view.drawToBitmap
import com.suda.yzune.wakeupschedule.AppDatabase
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.bean.*
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.CourseUtils
import com.suda.yzune.wakeupschedule.utils.ViewUtils
import com.suda.yzune.wakeupschedule.utils.getPrefer
import splitties.dimensions.dip
import java.text.ParseException

class TodayColorfulService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        if (intent != null) {
            val list = intent.data?.schemeSpecificPart?.split(",")
                    ?: return TodayColorfulRemoteViewsFactory()
            if (list.size < 2) {
                return TodayColorfulRemoteViewsFactory(nextDay = (list[0] == "1"))
            }
            return TodayColorfulRemoteViewsFactory(list[1].toInt(), list[0] == "1")
        } else {
            return TodayColorfulRemoteViewsFactory()
        }
    }

    private inner class TodayColorfulRemoteViewsFactory(val appWidgetId: Int = -1, val nextDay: Boolean = false) : RemoteViewsFactory {

        private val dataBase = AppDatabase.getDatabase(applicationContext)
        private val tableDao = dataBase.tableDao()
        private val courseDao = dataBase.courseDao()
        private val timeDao = dataBase.timeDetailDao()

        private var week = 1
        private lateinit var table: TableBean
        private lateinit var tableConfig: TableConfig
        private lateinit var styleConfig: WidgetStyleConfig
        private val timeList = arrayListOf<TimeDetailBean>()
        private val courseList = arrayListOf<CourseBean>()
        private var showColor = false
        private var screenInfo = arrayOf(1080, 1920)

        override fun onCreate() {

        }

        override fun onDataSetChanged() {
            // todo: 换成记录的id值而不是当前显示的
            if (appWidgetId == -1) return
            table = tableDao.getTableByIdSync(getPrefer().getInt(Const.KEY_SHOW_TABLE_ID, 1))
                    ?: return
            tableConfig = TableConfig(applicationContext, table.id)
            styleConfig = WidgetStyleConfig(applicationContext, appWidgetId)
            try {
                week = CourseUtils.countWeek(tableConfig.startDate, tableConfig.sundayFirst, nextDay)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            courseList.clear()
            if (week % 2 == 0) {
                courseList.addAll(courseDao.getCourseByDayOfTableSync(CourseUtils.getWeekdayInt(nextDay), week, 2, table.id))
            } else {
                courseList.addAll(courseDao.getCourseByDayOfTableSync(CourseUtils.getWeekdayInt(nextDay), week, 1, table.id))
            }
            timeList.clear()
            timeList.addAll(timeDao.getTimeListSync(table.timeTable))
            showColor = getPrefer().getBoolean(Const.KEY_DAY_WIDGET_COLOR, true)
            screenInfo = ViewUtils.getScreenInfo(applicationContext)
        }

        override fun onDestroy() {
            timeList.clear()
            courseList.clear()
        }

        override fun getCount(): Int {
            return if (courseList.isEmpty()) {
                1
            } else {
                courseList.size
            }
        }

        override fun getViewAt(position: Int): RemoteViews {
            val mRemoteViews = RemoteViews(applicationContext.packageName, R.layout.item_schedule_widget)
            if (position < 0) return mRemoteViews
            if (courseList.isNotEmpty()) {
                if (position >= courseList.size) return mRemoteViews
                val view = ViewUtils.initTodayCourseView(applicationContext, styleConfig, courseList[position], timeList)
                val contentView = view.findViewById<LinearLayout>(R.id.anko_layout)
                if (screenInfo[0] < screenInfo[1]) {
                    ViewUtils.layoutView(contentView, screenInfo[0], screenInfo[1])
                } else {
                    ViewUtils.layoutView(contentView, screenInfo[1], screenInfo[0])
                }
                val bitmap = ViewUtils.getViewBitmap(contentView, true, dip(2))
                mRemoteViews.setImageViewBitmap(R.id.iv_schedule, bitmap)
                contentView.removeAllViews()
            } else {
                val img = ImageView(applicationContext).apply {
                    setImageResource(R.drawable.ic_schedule_empty)
                }
                val view = LinearLayout(applicationContext).apply {
                    id = R.id.anko_empty_view
                    orientation = LinearLayout.VERTICAL
                    if (context.getPrefer().getBoolean(Const.KEY_SHOW_EMPTY_VIEW, true)) {
                        addView(img, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dip(180)).apply {
                            topMargin = dip(16)
                        })
                    }
                    addView(TextView(context).apply {
                        text = if (nextDay) {
                            "明天没有课哦"
                        } else {
                            "今天没有课哦"
                        }
                        setTextColor(styleConfig.textColor)
                        gravity = Gravity.CENTER
                    }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                        topMargin = dip(16)
                    })
                }
                if (screenInfo[0] < screenInfo[1]) {
                    ViewUtils.layoutView(view, screenInfo[0], screenInfo[1])
                } else {
                    ViewUtils.layoutView(view, screenInfo[1], screenInfo[0])
                }
                mRemoteViews.setImageViewBitmap(R.id.iv_schedule, view.drawToBitmap(Bitmap.Config.ARGB_4444))
            }
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
    }

}