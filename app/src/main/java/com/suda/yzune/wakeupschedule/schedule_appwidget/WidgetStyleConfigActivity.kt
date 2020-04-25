package com.suda.yzune.wakeupschedule.schedule_appwidget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.activity.viewModels
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseFragment
import com.suda.yzune.wakeupschedule.base_view.BaseTitleActivity
import com.suda.yzune.wakeupschedule.bean.TableConfig
import com.suda.yzune.wakeupschedule.bean.WidgetStyleConfig
import com.suda.yzune.wakeupschedule.utils.AppWidgetUtils
import com.suda.yzune.wakeupschedule.widget.colorpicker.ColorPickerFragment

class WidgetStyleConfigActivity : BaseTitleActivity(), ColorPickerFragment.ColorPickerDialogListener {

    private val viewModel by viewModels<WeekScheduleAppWidgetConfigViewModel>()

    override val layoutId: Int
        get() = R.layout.activity_suda_life

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.widgetId = intent.getIntExtra("widgetId", 0)
        viewModel.widgetConfig = WidgetStyleConfig(this, viewModel.widgetId)
        viewModel.tableConfig = TableConfig(this, viewModel.widgetConfig.tableId)
        val fragment = when (intent.getStringExtra("type")) {
            "today" -> TodayWidgetConfigFrag()
            else -> WeekWidgetConfigFrag()
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fl_fragment, fragment as BaseFragment, "current")
        transaction.commit()
    }

    override fun onStop() {
        super.onStop()
        AppWidgetUtils.updateWidget(applicationContext)
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        when (intent.getStringExtra("type")) {
            "today" -> AppWidgetUtils.refreshTodayWidget(applicationContext, appWidgetManager, viewModel.widgetId)
            else -> AppWidgetUtils.refreshScheduleWidget(applicationContext, appWidgetManager, viewModel.widgetId)
        }
        finish()
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        (supportFragmentManager.findFragmentByTag("current")
                as ColorPickerFragment.ColorPickerDialogListener).onColorSelected(dialogId, color)
    }
}