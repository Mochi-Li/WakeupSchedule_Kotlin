package com.suda.yzune.wakeupschedule.schedule_appwidget

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.IdRes
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.graphics.ColorUtils
import androidx.core.view.get
import androidx.core.view.setMargins
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseFragment
import com.suda.yzune.wakeupschedule.bean.DefaultValue
import com.suda.yzune.wakeupschedule.schedule.ScheduleUI
import com.suda.yzune.wakeupschedule.settings.SettingItemAdapter
import com.suda.yzune.wakeupschedule.settings.items.BaseSettingItem
import com.suda.yzune.wakeupschedule.settings.items.SeekBarItem
import com.suda.yzune.wakeupschedule.settings.items.SwitchItem
import com.suda.yzune.wakeupschedule.settings.items.VerticalItem
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.Const.BG_COLOR
import com.suda.yzune.wakeupschedule.utils.Const.COURSE_TEXT_COLOR
import com.suda.yzune.wakeupschedule.utils.Const.STROKE_COLOR
import com.suda.yzune.wakeupschedule.utils.Const.TITLE_COLOR
import com.suda.yzune.wakeupschedule.utils.CourseUtils
import com.suda.yzune.wakeupschedule.utils.Utils
import com.suda.yzune.wakeupschedule.utils.ViewUtils
import com.suda.yzune.wakeupschedule.widget.colorpicker.ColorPickerFragment
import kotlinx.coroutines.delay
import splitties.dimensions.dip
import splitties.resources.dimenPxSize
import splitties.resources.styledColor
import kotlin.math.min

class WeekWidgetConfigFrag : BaseFragment(), ColorPickerFragment.ColorPickerDialogListener {

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var frameLayout: FrameLayout
    private val viewModel by activityViewModels<WeekScheduleAppWidgetConfigViewModel>()
    private val mAdapter = SettingItemAdapter()

    private val widgetBgItem by lazy(LazyThreadSafetyMode.NONE) {
        VerticalItem(R.string.setting_widget_bg_color, "颜色跟透明度都可以哦\n长按恢复默认值")
    }

    private fun layout(constraintLayout: ConstraintLayout) {
        constraintLayout.apply {
            mRecyclerView = RecyclerView(context, null, R.attr.verticalRecyclerViewStyle).apply {
                id = R.id.rv_list
                overScrollMode = View.OVER_SCROLL_NEVER
                setBackgroundColor(styledColor(R.attr.colorSurface))
            }
            frameLayout = FrameLayout(context).apply {
                id = R.id.anko_layout
            }
            frameLayout.addView(View.inflate(context, R.layout.schedule_app_widget, null), FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT).apply {
                setMargins(requireContext().dip(8))
            })
            val screenInfo = ViewUtils.getScreenInfo(requireContext())
            if (screenInfo[0] < dimenPxSize(R.dimen.wide_screen) || screenInfo[0] < screenInfo[1]) {

                addView(frameLayout, ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT).apply {
                    topToTop = ConstraintSet.PARENT_ID
                    bottomToTop = R.id.rv_list
                    startToStart = ConstraintSet.PARENT_ID
                    endToEnd = ConstraintSet.PARENT_ID
                    matchConstraintPercentHeight = 0.4375f
                })

                addView(mRecyclerView, ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT).apply {
                    topToBottom = R.id.anko_layout
                    bottomToBottom = ConstraintSet.PARENT_ID
                    startToStart = ConstraintSet.PARENT_ID
                    endToEnd = ConstraintSet.PARENT_ID
                })
            } else {
                addView(frameLayout, ConstraintLayout.LayoutParams(
                        min(screenInfo[0], screenInfo[1]), ConstraintLayout.LayoutParams.MATCH_PARENT).apply {
                    topToTop = ConstraintSet.PARENT_ID
                    bottomToBottom = ConstraintSet.PARENT_ID
                    startToStart = ConstraintSet.PARENT_ID
                    endToStart = R.id.rv_list
                })

                addView(mRecyclerView, ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT).apply {
                    topToTop = ConstraintSet.PARENT_ID
                    bottomToBottom = ConstraintSet.PARENT_ID
                    startToEnd = R.id.anko_layout
                    endToEnd = ConstraintSet.PARENT_ID
                })
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ConstraintLayout(context).apply {
            layout(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        launch {
            loadSchedule()
        }
        val items = mutableListOf<BaseSettingItem>()
        onItemsCreated(items)
        mAdapter.data = items
        initEvent()
    }

    private fun onItemsCreated(items: MutableList<BaseSettingItem>) {
        items.add(VerticalItem(R.string.title_tips, "如果想调整小部件整体的高度，在这个页面是不行的！要回到桌面长按小部件来调整。华为和荣耀手机如果长按后调整不了，是第三方主题导致的，请切换回系统默认主题再调整。"))
        items.add(VerticalItem(R.string.setting_to_meizu_users, "首先非常感谢 Flyme 能在负一屏中适配本 App。不过调整小部件样式后，负一屏的显示不能马上生效，请点击小部件右上角的小箭头刷新小部件。另外关于「夜间模式」，颜色选择器可能会在 Flyme 提供的夜间模式中崩溃，请去系统设置中将本 App 排除夜间模式，App 已经适配了原生的夜间模式，应该是可以跟随开启的。"))
        items.add(VerticalItem(R.string.setting_widget_header_text_color, "指日期、节数等文字的颜色\n还可以调颜色的透明度哦 (●ﾟωﾟ●)"))
        items.add(VerticalItem(R.string.setting_course_text_color, "指课程格子内的颜色\n还可以调颜色的透明度哦 (●ﾟωﾟ●)"))
        items.add(VerticalItem(R.string.setting_stroke_color, "将不透明度调到最低就可以隐藏边框了哦"))
        items.add(SeekBarItem(R.string.setting_item_height, viewModel.widgetConfig.itemHeight, 32, 128, "dp"))
        items.add(SeekBarItem(R.string.setting_item_radius, viewModel.widgetConfig.radius, 0, 32, "dp"))
        items.add(SeekBarItem(R.string.setting_item_alpha, viewModel.widgetConfig.itemAlpha, 0, 100, "%"))
        items.add(SeekBarItem(R.string.setting_course_text_size, viewModel.widgetConfig.itemTextSize, 8, 16, "sp"))
        items.add(SwitchItem(R.string.setting_widget_show_bg, viewModel.widgetConfig.showBg))
        if (viewModel.widgetConfig.showBg) {
            items.add(widgetBgItem)
        }
        items.add(SwitchItem(R.string.setting_item_center_horizontal, viewModel.widgetConfig.itemCenterHorizontal))
        // items.add(SwitchItem("格子文字竖直居中", viewModel.widgetConfig.itemCenterVertical))
        items.add(SwitchItem(R.string.setting_item_show_time, viewModel.widgetConfig.showTime))
        items.add(SwitchItem(R.string.setting_item_show_teacher, viewModel.widgetConfig.showTeacher))
        items.add(SwitchItem(R.string.setting_show_time_bar, viewModel.widgetConfig.showTimeBar))
        items.add(SwitchItem(R.string.setting_show_sat, viewModel.widgetConfig.showSat))
        items.add(SwitchItem(R.string.setting_show_sun, viewModel.widgetConfig.showSun))
        items.add(SwitchItem(R.string.setting_show_other_week, viewModel.widgetConfig.showOtherWeekCourse))
        items.add(SwitchItem(R.string.setting_widget_show_date, viewModel.widgetConfig.showDate))
        items.add(VerticalItem(R.string.setting_blank, "\n\n\n"))
    }

    private fun <T : View> find(@IdRes id: Int): T {
        return frameLayout[0].findViewById(id)
    }

    private suspend fun loadSchedule() {

        if (viewModel.widgetConfig.showDate) {
            find<View>(R.id.tv_date).visibility = View.VISIBLE
        } else {
            find<View>(R.id.tv_date).visibility = View.GONE
        }

        if (viewModel.widgetConfig.showBg) {
            val space = requireContext().dip(8)
            find<View>(R.id.iv_appwidget).visibility = View.VISIBLE
            val bgColor = viewModel.widgetConfig.bgColor
            loadWidgetBg(bgColor)
            find<View>(R.id.rl_appwidget).setPadding(space, space * 2, space, space * 2)
        } else {
            find<View>(R.id.iv_appwidget).visibility = View.GONE
            find<View>(R.id.rl_appwidget).setPadding(0, 0, 0, 0)
        }
        find<TextView>(R.id.tv_date).textSize = viewModel.widgetConfig.itemTextSize.toFloat() + 2
        find<TextView>(R.id.tv_week).textSize = viewModel.widgetConfig.itemTextSize.toFloat()
        find<TextView>(R.id.tv_date).text = CourseUtils.getTodayDate()
        find<TextView>(R.id.tv_week).text = "${if (viewModel.tableConfig.tableName.isEmpty()) getString(R.string.my_schedule) else viewModel.tableConfig.tableName} | ${getString(R.string.week_num, 1)}    ${CourseUtils.getWeekday(requireContext())}"

        if (viewModel.widgetConfig.showSun) {
            if (viewModel.tableConfig.sundayFirst) {
                find<TextView>(R.id.tv_title7).visibility = View.GONE
                find<TextView>(R.id.tv_title0_1).visibility = View.VISIBLE
            } else {
                find<TextView>(R.id.tv_title7).visibility = View.VISIBLE
                find<TextView>(R.id.tv_title0_1).visibility = View.GONE
            }
        } else {
            find<TextView>(R.id.tv_title7).visibility = View.GONE
            find<TextView>(R.id.tv_title0_1).visibility = View.GONE
        }

        if (viewModel.widgetConfig.showSat) {
            find<TextView>(R.id.tv_title6).visibility = View.VISIBLE
        } else {
            find<TextView>(R.id.tv_title6).visibility = View.GONE
        }

        find<TextView>(R.id.tv_date).setTextColor(viewModel.widgetConfig.textColor)
        find<TextView>(R.id.tv_week).setTextColor(viewModel.widgetConfig.textColor)
        find<ImageView>(R.id.iv_next).setColorFilter(viewModel.widgetConfig.textColor)
        find<ImageView>(R.id.iv_back).setColorFilter(viewModel.widgetConfig.textColor)
        find<ImageView>(R.id.iv_settings).setColorFilter(viewModel.widgetConfig.textColor)

        find<TextView>(R.id.tv_title0).setTextColor(viewModel.widgetConfig.textColor)
        find<TextView>(R.id.tv_title0).textSize = viewModel.widgetConfig.itemTextSize.toFloat()

        val weekDate = CourseUtils.getDateStringFromWeek(CourseUtils.countWeek(viewModel.tableConfig.startDate, viewModel.tableConfig.sundayFirst), 1, viewModel.tableConfig.sundayFirst)
        find<TextView>(R.id.tv_title0).text = getString(R.string.main_month, weekDate[0])
        find<View>(R.id.iv_back).visibility = View.INVISIBLE

        if (viewModel.tableConfig.sundayFirst) {
            val day = 7
            for (i in 0..6) {
                if (i == day || (i == 0 && day == 7)) {
                    find<View>(R.id.weekName).findViewById<TextView>(R.id.tv_title0_1 + i).setTextColor(viewModel.widgetConfig.textColor)
                } else {
                    find<View>(R.id.weekName).findViewById<TextView>(R.id.tv_title0_1 + i).setTextColor((viewModel.widgetConfig.textColor and 0x00ffffff) + 0x33000000)
                }
                find<View>(R.id.weekName).findViewById<TextView>(R.id.tv_title0_1 + i).apply {
                    textSize = viewModel.widgetConfig.itemTextSize.toFloat()
                    text = viewModel.daysArray[i] + "\n${weekDate[i + 1]}"
                }
            }
        } else {
            val day = 1
            for (i in 0..6) {
                if (i == day - 1) {
                    find<View>(R.id.weekName).findViewById<TextView>(R.id.tv_title1 + i).setTextColor(viewModel.widgetConfig.textColor)
                } else {
                    find<View>(R.id.weekName).findViewById<TextView>(R.id.tv_title1 + i).setTextColor((viewModel.widgetConfig.textColor and 0x00ffffff) + 0x33000000)
                }
                find<View>(R.id.weekName).findViewById<TextView>(R.id.tv_title1 + i).apply {
                    textSize = viewModel.widgetConfig.itemTextSize.toFloat()
                    text = viewModel.daysArray[i + 1] + "\n${weekDate[i + 1]}"
                }
            }
        }

        val schedule = ScheduleUI(requireContext(), viewModel.tableConfig, viewModel.widgetConfig, 1, true)
        if (viewModel.timeList == null) {
            viewModel.initTimeList()
        }
        if (viewModel.timeList != null && schedule.showTimeDetail) {
            for (i in 0 until viewModel.tableConfig.nodes) {
                (schedule.content.getViewById(R.id.anko_tv_node1 + i) as LinearLayout).apply {
                    findViewById<TextView>(R.id.tv_start).text = viewModel.timeList!![i].startTime
                    findViewById<TextView>(R.id.tv_end).text = viewModel.timeList!![i].endTime
                }
            }
        }
        delay(100)
        for (i in 1..7) {
            schedule.initWeekPanel(viewModel.courseArray[i - 1], viewModel.timeList!!, 1, i)
        }
        val scrollView = schedule.scrollView
        val info = ViewUtils.getScreenInfo(requireContext())
        if (info[0] < info[1]) {
            ViewUtils.layoutView(scrollView, info[0], info[1])
        } else {
            ViewUtils.layoutView(scrollView, info[1], info[0])
        }
        val img = View.inflate(requireContext(), R.layout.item_schedule_widget, null).apply {
            this.findViewById<ImageView>(R.id.iv_schedule).setImageBitmap(ViewUtils.getViewBitmap(scrollView))
        }
        find<ListView>(R.id.lv_schedule).adapter = object : BaseAdapter() {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                return img
            }

            override fun getItem(position: Int): Any {
                return 1
            }

            override fun getItemId(position: Int): Long {
                return position.toLong()
            }

            override fun getCount(): Int {
                return 1
            }

        }
        scrollView.removeAllViews()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val v = (requireView() as ConstraintLayout)
        v.removeAllViews()
        layout(v)
        launch { loadSchedule() }
        initEvent()
    }

    private fun initEvent() {
        mRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        mRecyclerView.itemAnimator?.changeDuration = 250
        mRecyclerView.adapter = mAdapter
        mAdapter.addChildClickViewIds(R.id.anko_check_box)
        mAdapter.setOnItemChildClickListener { adapter, itemView, position ->
            when (val item = adapter.data[position]) {
                is SwitchItem -> onSwitchItemCheckChange(item, itemView.findViewById<AppCompatCheckBox>(R.id.anko_check_box).isChecked, position)
            }
        }
        mAdapter.setOnItemClickListener { adapter, view, position ->
            when (val item = adapter.data[position]) {
                is VerticalItem -> onVerticalItemClick(item)
                is SwitchItem -> view.findViewById<AppCompatCheckBox>(R.id.anko_check_box).performClick()
                is SeekBarItem -> onSeekBarItemClick(item, position)
            }
        }
        mAdapter.setOnItemLongClickListener { adapter, _, position ->
            when (val item = adapter.data[position]) {
                is VerticalItem -> onVerticalItemLongClick(item)
            }
            true
        }
    }

    private fun onSeekBarItemClick(item: SeekBarItem, position: Int) {
        Utils.showSeekBarItemDialog(requireContext(), item) { dialog, value ->
            when (item.title) {
                R.string.setting_item_height -> viewModel.widgetConfig.itemHeight = value
                R.string.setting_item_alpha -> viewModel.widgetConfig.itemAlpha = value
                R.string.setting_course_text_size -> viewModel.widgetConfig.itemTextSize = value
                R.string.setting_item_radius -> viewModel.widgetConfig.radius = value
            }
            item.valueInt = value
            mAdapter.notifyItemChanged(position)
            launch { loadSchedule() }
            dialog.dismiss()
        }
    }

    private fun onSwitchItemCheckChange(item: SwitchItem, isChecked: Boolean, position: Int) {
        when (item.title) {
            R.string.setting_show_sat -> viewModel.widgetConfig.showSat = isChecked
            R.string.setting_show_sun -> viewModel.widgetConfig.showSun = isChecked
            R.string.setting_item_show_time -> viewModel.widgetConfig.showTime = isChecked
            R.string.setting_item_show_teacher -> viewModel.widgetConfig.showTeacher = isChecked
            R.string.setting_show_other_week -> viewModel.widgetConfig.showOtherWeekCourse = isChecked
            R.string.setting_item_center_horizontal -> viewModel.widgetConfig.itemCenterHorizontal = isChecked
//            "格子文字竖直居中" -> viewModel.widgetConfig.itemCenterVertical = isChecked
            R.string.setting_show_time_bar -> viewModel.widgetConfig.showTimeBar = isChecked
            R.string.setting_widget_show_bg -> {
                viewModel.widgetConfig.showBg = isChecked
                if (isChecked) {
                    mAdapter.addData(position + 1, widgetBgItem)
                } else {
                    mAdapter.remove(widgetBgItem)
                }
            }
            R.string.setting_widget_show_date -> viewModel.widgetConfig.showDate = isChecked
        }
        item.checked = isChecked
        launch { loadSchedule() }
    }

    private fun onVerticalItemClick(item: VerticalItem) {
        when (item.title) {
            R.string.setting_widget_header_text_color -> {
                Utils.buildColorPickerDialogBuilder(requireActivity(), viewModel.widgetConfig.textColor, TITLE_COLOR)
            }
            R.string.setting_course_text_color -> {
                Utils.buildColorPickerDialogBuilder(requireActivity(), viewModel.widgetConfig.courseTextColor, COURSE_TEXT_COLOR)
            }
            R.string.setting_stroke_color -> {
                Utils.buildColorPickerDialogBuilder(requireActivity(), viewModel.widgetConfig.strokeColor, STROKE_COLOR)
            }
            R.string.setting_widget_bg_color -> {
                Utils.buildColorPickerDialogBuilder(requireActivity(), viewModel.widgetConfig.bgColor, BG_COLOR)
            }
        }
    }

    private fun onVerticalItemLongClick(item: VerticalItem): Boolean {
        return when (item.title) {
            R.string.setting_widget_bg_color -> {
                viewModel.widgetConfig.bgColor = DefaultValue.widgetBgColor
                loadWidgetBg(DefaultValue.widgetBgColor)
                true
            }
            else -> false
        }
    }

    private fun loadWidgetBg(bgColor: Int) {
        find<ImageView>(R.id.iv_appwidget).imageAlpha = Color.alpha(bgColor)
        find<ImageView>(R.id.iv_appwidget).setColorFilter(ColorUtils.setAlphaComponent(bgColor, 255))
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        val newColor = when (dialogId) {
            TITLE_COLOR, COURSE_TEXT_COLOR -> {
                if (Color.alpha(color) < Const.MIN_TEXT_COLOR_ALPHA) {
                    ColorUtils.setAlphaComponent(color, Const.MIN_TEXT_COLOR_ALPHA)
                } else color
            }
            else -> color
        }
        when (dialogId) {
            TITLE_COLOR -> viewModel.widgetConfig.textColor = newColor
            COURSE_TEXT_COLOR -> viewModel.widgetConfig.courseTextColor = newColor
            STROKE_COLOR -> viewModel.widgetConfig.strokeColor = newColor
            BG_COLOR -> {
                viewModel.widgetConfig.bgColor = newColor
                loadWidgetBg(newColor)
            }
        }
        launch { loadSchedule() }
    }

}