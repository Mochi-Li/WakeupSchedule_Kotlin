package com.suda.yzune.wakeupschedule.schedule_appwidget

import android.appwidget.AppWidgetManager
import android.graphics.Typeface
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatTextView
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseFragment
import com.suda.yzune.wakeupschedule.base_view.BaseTitleActivity
import com.suda.yzune.wakeupschedule.bean.TableConfig
import com.suda.yzune.wakeupschedule.bean.WidgetStyleConfig
import com.suda.yzune.wakeupschedule.utils.AppWidgetUtils
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.getPrefer
import com.suda.yzune.wakeupschedule.widget.colorpicker.ColorPickerFragment
import es.dmoral.toasty.Toasty
import splitties.resources.color

class WidgetStyleConfigActivity : BaseTitleActivity(), ColorPickerFragment.ColorPickerDialogListener {

    private val viewModel by viewModels<WeekScheduleAppWidgetConfigViewModel>()

    override val layoutId: Int
        get() = R.layout.activity_suda_life

    override fun onSetupSubButton(): AppCompatTextView? {
        val tvButton = AppCompatTextView(this)
        tvButton.text = "以此为默认样式"
        tvButton.typeface = Typeface.DEFAULT_BOLD
        tvButton.setTextColor(color(R.color.colorAccent))
        tvButton.setOnClickListener {
            saveDefault()
        }
        return tvButton
    }

    private fun saveDefault() {
        when (intent.getStringExtra("type")) {
            "today" -> {
                WidgetStyleConfig(this, -1).copy(viewModel.widgetConfig)
            }
            else -> {
                WidgetStyleConfig(this, -2).copy(viewModel.widgetConfig)
            }
        }
        Toasty.success(this, "设置成功").show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.widgetId = intent.getIntExtra("widgetId", 0)
        viewModel.widgetConfig = WidgetStyleConfig(this, viewModel.widgetId)
        val fragment = when (intent.getStringExtra("type")) {
            "today" -> {
                viewModel.tableConfig = TableConfig(this, getPrefer().getInt(Const.KEY_SHOW_TABLE_ID, 1))
                TodayWidgetConfigFrag()
            }
            else -> {
                viewModel.tableConfig = TableConfig(this, viewModel.widgetConfig.tableId)
                WeekWidgetConfigFrag()
            }
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