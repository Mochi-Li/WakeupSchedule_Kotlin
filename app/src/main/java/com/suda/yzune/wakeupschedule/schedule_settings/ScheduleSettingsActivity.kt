package com.suda.yzune.wakeupschedule.schedule_settings

import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.suda.yzune.wakeupschedule.BuildConfig
import com.suda.yzune.wakeupschedule.DonateActivity
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseTitleActivity
import com.suda.yzune.wakeupschedule.bean.TableBean
import com.suda.yzune.wakeupschedule.bean.TableConfig
import com.suda.yzune.wakeupschedule.schedule.DonateFragment
import com.suda.yzune.wakeupschedule.utils.AppWidgetUtils
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.getPrefer
import com.suda.yzune.wakeupschedule.widget.colorpicker.ColorPickerFragment
import splitties.activities.start

class ScheduleSettingsActivity : BaseTitleActivity(), ColorPickerFragment.ColorPickerDialogListener {

    private val viewModel by viewModels<ScheduleSettingsViewModel>()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.table = intent.extras!!.getParcelable<TableBean>("tableData") as TableBean
        viewModel.tableConfig = TableConfig(this, viewModel.table.id)
        navController = Navigation.findNavController(this, R.id.nav_fragment)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            mainTitle.text = destination.label
        }
        intent.extras?.getInt("action", 0)?.let {
            if (it == 0) return@let
            val bundle = Bundle()
            bundle.putString("settingItem", intent.extras?.getString("settingItem"))
            navController.navigate(it, bundle)
        }
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        (getForegroundFragment() as ColorPickerFragment.ColorPickerDialogListener).onColorSelected(dialogId, color)
    }

    private fun getForegroundFragment(): Fragment {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_fragment)
        return navHostFragment?.childFragmentManager?.fragments!![0]
    }

    override fun onSetupSubButton(): AppCompatImageButton? {
        val tvButton = AppCompatImageButton(this).apply {
            setImageResource(R.drawable.ic_outline_favorite_border_24)
        }
        if (BuildConfig.CHANNEL == "google" || (BuildConfig.CHANNEL == "huawei" && !getPrefer().getBoolean(Const.KEY_SHOW_DONATE, false))) {
            tvButton.setOnClickListener {
                val dialog = DonateFragment.newInstance()
                dialog.show(supportFragmentManager, "donateDialog")
            }
        } else {
            tvButton.setOnClickListener {
                start<DonateActivity>()
            }
        }
        return tvButton
    }

    override val layoutId: Int
        get() = R.layout.activity_settings_host

    override fun onBackPressed() {
        when (navController.currentDestination?.id) {
            R.id.scheduleSettingsFragment -> {
                launch {
                    AppWidgetUtils.updateWidget(applicationContext)
                    val list = viewModel.getScheduleWidgetIds()
                    val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
                    list.forEach {
                        when (it.detailType) {
                            0 -> {
                                if (it.info == viewModel.table.id.toString()) {
                                    AppWidgetUtils.refreshScheduleWidget(applicationContext, appWidgetManager, it.id)
                                }
                            }
                            1 -> AppWidgetUtils.refreshTodayWidget(applicationContext, appWidgetManager, it.id)
                        }
                    }
                    setResult(RESULT_OK)
                    finish()
                }
            }
            else -> {
                super.onBackPressed()
            }
        }
    }
}
