package com.suda.yzune.wakeupschedule.utils

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.core.graphics.ColorUtils
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.SplashActivity
import com.suda.yzune.wakeupschedule.bean.TableConfig
import com.suda.yzune.wakeupschedule.bean.WidgetStyleConfig
import com.suda.yzune.wakeupschedule.schedule_appwidget.ScheduleAppWidget
import com.suda.yzune.wakeupschedule.schedule_appwidget.ScheduleAppWidgetService
import com.suda.yzune.wakeupschedule.schedule_appwidget.WidgetStyleConfigActivity
import com.suda.yzune.wakeupschedule.today_appwidget.TodayColorfulService
import com.suda.yzune.wakeupschedule.today_appwidget.TodayCourseAppWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import splitties.dimensions.dip

fun BroadcastReceiver.goAsync(
        coroutineScope: CoroutineScope = GlobalScope,
        block: suspend () -> Unit
) {
    val result = goAsync()
    coroutineScope.launch {
        try {
            block()
        } finally {
            // Always call finish(), even if the coroutineScope was cancelled
            result.finish()
        }
    }
}

object AppWidgetUtils {
    private val daysArray = arrayOf("日", "一", "二", "三", "四", "五", "六", "日")

    fun updateWidget(context: Context) {
        val intent = Intent()
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        context.sendBroadcast(intent)
    }

    fun refreshScheduleWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, nextWeek: Boolean = false) {
        val mRemoteViews = RemoteViews(context.packageName, R.layout.schedule_app_widget)
        val config = WidgetStyleConfig(context, appWidgetId)
        val tableBean = TableConfig(context, config.tableId)
        var week = CourseUtils.countWeek(tableBean.startDate, tableBean.sundayFirst)
        if (nextWeek) {
            week++
        }
        val date = CourseUtils.getTodayDate()
        val weekDay = CourseUtils.getWeekday()
        if (config.showDate) {
            mRemoteViews.setViewVisibility(R.id.tv_date, View.VISIBLE)
        } else {
            mRemoteViews.setViewVisibility(R.id.tv_date, View.GONE)
        }
        if (config.showBg) {
            val space = context.dip(8)
            mRemoteViews.setViewVisibility(R.id.iv_appwidget, View.VISIBLE)
            val bgColor = config.bgColor
            mRemoteViews.setInt(R.id.iv_appwidget, "setImageAlpha", Color.alpha(bgColor))
            mRemoteViews.setInt(R.id.iv_appwidget, "setColorFilter", ColorUtils.setAlphaComponent(bgColor, 255))
            mRemoteViews.setViewPadding(R.id.rl_appwidget, space, space * 2, space, space * 2)
        } else {
            mRemoteViews.setViewVisibility(R.id.iv_appwidget, View.GONE)
            mRemoteViews.setViewPadding(R.id.rl_appwidget, 0, 0, 0, 0)
        }
        mRemoteViews.setTextViewTextSize(R.id.tv_date, TypedValue.COMPLEX_UNIT_SP, config.itemTextSize.toFloat() + 2)
        mRemoteViews.setTextViewTextSize(R.id.tv_week, TypedValue.COMPLEX_UNIT_SP, config.itemTextSize.toFloat())
        mRemoteViews.setTextViewText(R.id.tv_date, date)
        if (tableBean.tableName.isEmpty()) {
            tableBean.tableName = "我的课表"
        }
        var notStart = false
        if (week > 0) {
            if (nextWeek) {
                mRemoteViews.setTextViewText(R.id.tv_week, "${tableBean.tableName} | 第${week}周")
            } else {
                mRemoteViews.setTextViewText(R.id.tv_week, "${tableBean.tableName} | 第${week}周    $weekDay")
            }
        } else {
            mRemoteViews.setTextViewText(R.id.tv_week, "${tableBean.tableName} | 还没有开学哦")
            week = 1
            notStart = true
        }

        if (config.showSun) {
            if (tableBean.sundayFirst) {
                mRemoteViews.setViewVisibility(R.id.tv_title7, View.GONE)
                mRemoteViews.setViewVisibility(R.id.tv_title0_1, View.VISIBLE)
            } else {
                mRemoteViews.setViewVisibility(R.id.tv_title7, View.VISIBLE)
                mRemoteViews.setViewVisibility(R.id.tv_title0_1, View.GONE)
            }
        } else {
            mRemoteViews.setViewVisibility(R.id.tv_title7, View.GONE)
            mRemoteViews.setViewVisibility(R.id.tv_title0_1, View.GONE)
        }

        if (config.showSat) {
            mRemoteViews.setViewVisibility(R.id.tv_title6, View.VISIBLE)
        } else {
            mRemoteViews.setViewVisibility(R.id.tv_title6, View.GONE)
        }

        mRemoteViews.setTextColor(R.id.tv_date, config.textColor)
        mRemoteViews.setTextColor(R.id.tv_week, config.textColor)
        mRemoteViews.setInt(R.id.iv_next, "setColorFilter", config.textColor)
        mRemoteViews.setInt(R.id.iv_back, "setColorFilter", config.textColor)
        mRemoteViews.setInt(R.id.iv_settings, "setColorFilter", config.textColor)
        val weekDate = CourseUtils.getDateStringFromWeek(CourseUtils.countWeek(tableBean.startDate, tableBean.sundayFirst), week, tableBean.sundayFirst)
        mRemoteViews.setTextColor(R.id.tv_title0, config.textColor)
        mRemoteViews.setTextViewTextSize(R.id.tv_title0, TypedValue.COMPLEX_UNIT_SP, config.itemTextSize.toFloat())
        mRemoteViews.setTextViewText(R.id.tv_title0, weekDate[0] + "\n月")
        if (nextWeek) {
            if (!notStart) {
                mRemoteViews.setTextViewText(R.id.tv_date, "下周")
            }
            mRemoteViews.setViewVisibility(R.id.iv_next, View.INVISIBLE)
            mRemoteViews.setViewVisibility(R.id.iv_back, View.VISIBLE)
        } else {
            mRemoteViews.setTextViewText(R.id.tv_date, date)
            mRemoteViews.setViewVisibility(R.id.iv_next, View.VISIBLE)
            mRemoteViews.setViewVisibility(R.id.iv_back, View.INVISIBLE)
        }

        val day = CourseUtils.getWeekdayInt()

        if (tableBean.sundayFirst) {
            for (i in 0..6) {
                if (i == day || (i == 0 && day == 7)) {
                    mRemoteViews.setTextColor(R.id.tv_title0_1 + i, config.textColor)
                } else {
                    mRemoteViews.setTextColor(R.id.tv_title0_1 + i, (config.textColor and 0x00ffffff) + 0x33000000)
                }
                mRemoteViews.setTextViewTextSize(R.id.tv_title0_1 + i, TypedValue.COMPLEX_UNIT_SP, config.itemTextSize.toFloat())
                mRemoteViews.setTextViewText(R.id.tv_title0_1 + i, daysArray[i] + "\n${weekDate[i + 1]}")
            }
        } else {
            for (i in 0..6) {
                if (i == day - 1) {
                    mRemoteViews.setTextColor(R.id.tv_title1 + i, config.textColor)
                } else {
                    mRemoteViews.setTextColor(R.id.tv_title1 + i, (config.textColor and 0x00ffffff) + 0x33000000)
                }
                mRemoteViews.setTextViewTextSize(R.id.tv_title1 + i, TypedValue.COMPLEX_UNIT_SP, config.itemTextSize.toFloat())
                mRemoteViews.setTextViewText(R.id.tv_title1 + i, daysArray[i + 1] + "\n${weekDate[i + 1]}")
            }
        }
        val lvIntent = Intent(context, ScheduleAppWidgetService::class.java)
        lvIntent.data = if (nextWeek) {
            Uri.fromParts("content", "1,${appWidgetId}", null)
        } else {
            Uri.fromParts("content", "0,${appWidgetId}", null)
        }
        mRemoteViews.setRemoteAdapter(R.id.lv_schedule, lvIntent)
        val intent = Intent(context, SplashActivity::class.java)
        val pIntent = PendingIntent.getActivity(context, 0, intent, 0)
        mRemoteViews.setOnClickPendingIntent(R.id.rl_appwidget, pIntent)

        val nextIntent = Intent(context, ScheduleAppWidget::class.java)
        nextIntent.action = "WAKEUP_NEXT_WEEK"
        val pi = PendingIntent.getBroadcast(context, 1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        mRemoteViews.setOnClickPendingIntent(R.id.iv_next, pi)

        val backIntent = Intent(context, ScheduleAppWidget::class.java)
        backIntent.action = "WAKEUP_BACK_WEEK"
        val backPi = PendingIntent.getBroadcast(context, 2, backIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        mRemoteViews.setOnClickPendingIntent(R.id.iv_back, backPi)

        val configIntent = Intent(context, WidgetStyleConfigActivity::class.java).apply {
            putExtra("type", "week")
            putExtra("widgetId", appWidgetId)
        }
        val configPi = PendingIntent.getActivity(context, appWidgetId, configIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        mRemoteViews.setOnClickPendingIntent(R.id.iv_settings, configPi)

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.lv_schedule)
        appWidgetManager.updateAppWidget(appWidgetId, mRemoteViews)
    }

    fun refreshTodayWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, nextDay: Boolean = false) {
        val mRemoteViews = RemoteViews(context.packageName, R.layout.today_course_app_widget)
        val config = WidgetStyleConfig(context, appWidgetId)
        val tableBean = TableConfig(context, context.getPrefer().getInt(Const.KEY_SHOW_TABLE_ID, 1))
        val week = CourseUtils.countWeek(tableBean.startDate, tableBean.sundayFirst, nextDay)
        val date = CourseUtils.getTodayDate()
        val weekDay = CourseUtils.getWeekday(nextDay)
        if (config.showDate) {
            mRemoteViews.setViewVisibility(R.id.tv_date, View.VISIBLE)
        } else {
            mRemoteViews.setViewVisibility(R.id.tv_date, View.GONE)
        }
        if (config.showBg) {
            val space = context.dip(8)
            mRemoteViews.setViewVisibility(R.id.iv_appwidget, View.VISIBLE)
            val bgColor = config.bgColor
            mRemoteViews.setInt(R.id.iv_appwidget, "setImageAlpha", Color.alpha(bgColor))
            mRemoteViews.setInt(R.id.iv_appwidget, "setColorFilter", ColorUtils.setAlphaComponent(bgColor, 255))
            mRemoteViews.setViewPadding(R.id.rl_appwidget, space, space * 2, space, space * 2)
        } else {
            mRemoteViews.setViewVisibility(R.id.iv_appwidget, View.GONE)
            mRemoteViews.setViewPadding(R.id.rl_appwidget, 0, 0, 0, 0)
        }
        mRemoteViews.setTextColor(R.id.tv_date, config.textColor)
        mRemoteViews.setTextColor(R.id.tv_week, config.textColor)
        mRemoteViews.setInt(R.id.iv_next, "setColorFilter", config.textColor)
        mRemoteViews.setInt(R.id.iv_back, "setColorFilter", config.textColor)
        mRemoteViews.setInt(R.id.iv_settings, "setColorFilter", config.textColor)
        mRemoteViews.setTextViewTextSize(R.id.tv_date, TypedValue.COMPLEX_UNIT_DIP, config.itemTextSize.toFloat() + 2)
        mRemoteViews.setTextViewTextSize(R.id.tv_week, TypedValue.COMPLEX_UNIT_DIP, config.itemTextSize.toFloat())
        if (nextDay) {
            mRemoteViews.setTextViewText(R.id.tv_date, "明天")
            mRemoteViews.setViewVisibility(R.id.iv_next, View.INVISIBLE)
            mRemoteViews.setViewVisibility(R.id.iv_back, View.VISIBLE)
        } else {
            mRemoteViews.setTextViewText(R.id.tv_date, date)
            mRemoteViews.setViewVisibility(R.id.iv_next, View.VISIBLE)
            mRemoteViews.setViewVisibility(R.id.iv_back, View.INVISIBLE)
        }
        if (week > 0) {
            mRemoteViews.setTextViewText(R.id.tv_week, "第${week}周    $weekDay")
        } else {
            mRemoteViews.setTextViewText(R.id.tv_week, "还没有开学哦")
        }
        val lvIntent = Intent(context, TodayColorfulService::class.java)

        lvIntent.data = if (nextDay) {
            Uri.fromParts("content", "1,${appWidgetId}", null)
        } else {
            Uri.fromParts("content", "0,${appWidgetId}", null)
        }
        mRemoteViews.setRemoteAdapter(R.id.lv_course, lvIntent)
        val intent = Intent(context, SplashActivity::class.java)
        val pIntent = PendingIntent.getActivity(context, 0, intent, 0)
        mRemoteViews.setOnClickPendingIntent(R.id.rl_appwidget, pIntent)

        val i = Intent(context, TodayCourseAppWidget::class.java)
        i.action = "WAKEUP_NEXT_DAY"
        val pi = PendingIntent.getBroadcast(context, 1, i, PendingIntent.FLAG_UPDATE_CURRENT)
        mRemoteViews.setOnClickPendingIntent(R.id.iv_next, pi)

        val backIntent = Intent(context, TodayCourseAppWidget::class.java)
        backIntent.action = "WAKEUP_BACK_TIME"
        val backPi = PendingIntent.getBroadcast(context, 2, backIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        mRemoteViews.setOnClickPendingIntent(R.id.iv_back, backPi)

        val configIntent = Intent(context, WidgetStyleConfigActivity::class.java).apply {
            putExtra("type", "today")
            putExtra("widgetId", appWidgetId)
        }
        val configPi = PendingIntent.getActivity(context, appWidgetId, configIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        mRemoteViews.setOnClickPendingIntent(R.id.iv_settings, configPi)

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.lv_course)
        appWidgetManager.updateAppWidget(appWidgetId, mRemoteViews)
    }
}