package com.suda.yzune.wakeupschedule.schedule_appwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseBlurTitleActivity
import com.suda.yzune.wakeupschedule.bean.AppWidgetBean
import com.suda.yzune.wakeupschedule.bean.TableConfig
import com.suda.yzune.wakeupschedule.bean.WidgetStyleConfig
import com.suda.yzune.wakeupschedule.utils.AppWidgetUtils
import com.suda.yzune.wakeupschedule.utils.ViewUtils
import kotlinx.android.synthetic.main.activity_week_schedule_app_widget_config.*
import splitties.resources.dimenPxSize

class WeekScheduleAppWidgetConfigActivity : BaseBlurTitleActivity() {

    override val layoutId: Int
        get() = R.layout.activity_week_schedule_app_widget_config

    override fun onSetupSubButton(tvButton: AppCompatTextView): AppCompatTextView? {
        return null
    }

    private val viewModel by viewModels<WeekScheduleAppWidgetConfigViewModel>()
    private var mAppWidgetId = 0
    private var isTodayType = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID)
        }

        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        //Log.d("包名", appWidgetManager.getAppWidgetInfo(mAppWidgetId).provider.shortClassName)
        val what = appWidgetManager.getAppWidgetInfo(mAppWidgetId).provider.shortClassName
        isTodayType = (what == ".today_appwidget.TodayCourseAppWidget" || what == "com.suda.yzune.wakeupschedule.today_appwidget.TodayCourseAppWidget")
        if (isTodayType) {
//            Glide.with(this)
//                    .load("https://ws2.sinaimg.cn/large/0069RVTdgy1fv5ypjuqs1j30u01hcdlt.jpg")
//                    .transition(DrawableTransitionOptions.withCrossFade())
//                    .into(iv_tip)
        } else {
            tv_got_it.visibility = View.GONE
            val list = ArrayList<TableConfig>()
            val adapter = WidgetTableListAdapter(R.layout.item_table_list, list)
            adapter.setOnItemClickListener { _, _, position ->
                launch {
                    viewModel.insertWeekAppWidgetData(AppWidgetBean(mAppWidgetId, 0, 0, ""))
                    WidgetStyleConfig(applicationContext, mAppWidgetId).tableId = list[position].id
                    AppWidgetUtils.refreshScheduleWidget(applicationContext, appWidgetManager, mAppWidgetId)
                    val resultValue = Intent()
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
                    setResult(Activity.RESULT_OK, resultValue)
                    finish()
                }
            }
            rv_list.adapter = adapter
            if (ViewUtils.getScreenInfo(this)[0] < dimenPxSize(R.dimen.wide_screen)) {
                rv_list.layoutManager = LinearLayoutManager(this)
            } else {
                rv_list.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
            }
            launch {
                list.clear()
                list.addAll(viewModel.getTableList().map { tableBean ->
                    TableConfig(this@WeekScheduleAppWidgetConfigActivity, tableBean.id)
                })
                adapter.notifyDataSetChanged()
            }
        }

        tv_got_it.setOnClickListener {
            launch {
                // Log.d("包名", appWidgetManager.getAppWidgetInfo(mAppWidgetId).provider.shortClassName)
                viewModel.insertWeekAppWidgetData(AppWidgetBean(mAppWidgetId, 0, 1, ""))
                AppWidgetUtils.refreshTodayWidget(applicationContext, appWidgetManager, mAppWidgetId)
                val resultValue = Intent()
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
                setResult(Activity.RESULT_OK, resultValue)
                finish()
            }
        }
    }

    override fun onBackPressed() {
        if (isTodayType) {
            MaterialAlertDialogBuilder(this)
                    .setTitle("提示")
                    .setMessage("请仔细阅读提示并点「我知道啦」按钮")
                    .setPositiveButton("确定", null)
                    .setNegativeButton("取消放置小部件") { _, _ ->
                        finish()
                    }
                    .show()
        } else {
            MaterialAlertDialogBuilder(this)
                    .setTitle("提示")
                    .setMessage("请从列表中选择需要放置的课表")
                    .setPositiveButton("我知道啦", null)
                    .setNegativeButton("取消放置小部件") { _, _ ->
                        finish()
                    }
                    .show()
        }
    }
}
