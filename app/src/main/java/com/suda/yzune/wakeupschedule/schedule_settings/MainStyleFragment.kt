package com.suda.yzune.wakeupschedule.schedule_settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseFragment
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
import com.suda.yzune.wakeupschedule.utils.Utils.buildColorPickerDialogBuilder
import com.suda.yzune.wakeupschedule.utils.ViewUtils
import com.suda.yzune.wakeupschedule.widget.colorpicker.ColorPickerFragment
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okio.BufferedSink
import okio.BufferedSource
import okio.Okio
import splitties.dimensions.dip
import splitties.resources.dimenPxSize
import java.io.File
import kotlin.math.min

class MainStyleFragment : BaseFragment(), ColorPickerFragment.ColorPickerDialogListener {

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var schedule: ScheduleUI
    private lateinit var bg: ImageView
    private lateinit var frameLayout: FrameLayout
    private val viewModel by activityViewModels<ScheduleSettingsViewModel>()
    private val mAdapter = SettingItemAdapter()

    private fun layout(constraintLayout: ConstraintLayout) {
        constraintLayout.apply {
            mRecyclerView = RecyclerView(context, null, R.attr.verticalRecyclerViewStyle).apply {
                id = R.id.rv_list
                overScrollMode = View.OVER_SCROLL_NEVER
            }

            bg = ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            frameLayout = FrameLayout(context).apply {
                id = R.id.anko_layout
            }

            frameLayout.addView(bg, FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
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
        loadBg()
        val items = mutableListOf<BaseSettingItem>()
        onItemsCreated(items)
        mAdapter.data = items
        initEvent()
        val settingItem = arguments?.getString("settingItem")
        if (settingItem != null && savedInstanceState == null) {
            mRecyclerView.postDelayed({
                try {
                    val i = items.indexOfFirst {
                        it.title == settingItem
                    }
                    (mRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(i, requireContext().dip(64))
                    when (items[i]) {
                        is VerticalItem -> onVerticalItemClick(items[i] as VerticalItem)
                        is SeekBarItem -> onSeekBarItemClick(items[i] as SeekBarItem, i)
                    }
                } catch (e: Exception) {

                }
            }, 100)
        }
    }

    private fun onItemsCreated(items: MutableList<BaseSettingItem>) {
        items.add(VerticalItem("课程表背景", "长按可以恢复默认哦~"))
        items.add(VerticalItem("界面文字颜色", "指标题等字体的颜色\n还可以调颜色的透明度哦 (●ﾟωﾟ●)"))
        items.add(VerticalItem("课程文字颜色", "指课程格子内的颜色\n还可以调颜色的透明度哦 (●ﾟωﾟ●)"))
        items.add(VerticalItem("格子边框颜色", "将不透明度调到最低就可以隐藏边框了哦"))
        items.add(SeekBarItem("格子高度", viewModel.tableConfig.itemHeight, 32, 128, "dp"))
        items.add(SeekBarItem("格子圆角半径", viewModel.tableConfig.radius, 0, 32, "dp"))
        items.add(SeekBarItem("格子不透明度", viewModel.tableConfig.itemAlpha, 0, 100, "%"))
        items.add(SeekBarItem("格子文字大小", viewModel.tableConfig.itemTextSize, 8, 16, "sp"))
        items.add(SwitchItem("格子文字水平居中", viewModel.tableConfig.itemCenterHorizontal))
        // items.add(SwitchItem("格子文字竖直居中", viewModel.tableConfig.itemCenterVertical))
        items.add(SwitchItem("在格子内显示上课时间", viewModel.tableConfig.showTime))
        items.add(SwitchItem("在格子内显示授课老师", viewModel.tableConfig.showTeacher))
        items.add(SwitchItem("节数栏显示时间", viewModel.tableConfig.showTimeBar))
        items.add(SwitchItem("显示周六", viewModel.tableConfig.showSat))
        items.add(SwitchItem("显示周日", viewModel.tableConfig.showSun))
        items.add(SwitchItem("显示非本周课程", viewModel.tableConfig.showOtherWeekCourse))
        items.add(VerticalItem("", "\n\n\n"))
    }

    private suspend fun loadSchedule() {
        if (frameLayout.childCount > 1) {
            frameLayout.removeView(schedule.root)
        }
        schedule = ScheduleUI(requireContext(), viewModel.tableConfig, viewModel.tableConfig, 1)
        frameLayout.addView(schedule.root, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        val weekDate = CourseUtils.getDateStringFromWeek(CourseUtils.countWeek(viewModel.tableConfig.startDate, viewModel.tableConfig.sundayFirst), 1, viewModel.tableConfig.sundayFirst)
        (schedule.root.getViewById(R.id.anko_tv_title0) as TextView).text = weekDate[0] + "\n月"
        var textView: TextView?
        for (i in 1..7) {
            if (schedule.dayMap[i] == -1) continue
            textView = schedule.root.getViewById(R.id.anko_tv_title0 + schedule.dayMap[i]) as TextView
            if (i == 7 && !viewModel.tableConfig.showSat && !viewModel.tableConfig.sundayFirst) {
                textView.text = viewModel.daysArray[i] + "\n${weekDate[7]}"
            } else if (!viewModel.tableConfig.showSun && viewModel.tableConfig.sundayFirst && i != 7) {
                textView.text = viewModel.daysArray[i] + "\n${weekDate[schedule.dayMap[i] + 1]}"
            } else {
                textView.text = viewModel.daysArray[i] + "\n${weekDate[schedule.dayMap[i]]}"
            }
        }
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
    }

    private fun loadBg() {
        if (viewModel.tableConfig.background != "") {
            //ui.bg.clearColorFilter()
            if (viewModel.tableConfig.background.startsWith("#")) {
                val bitmap = try {
                    Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888).apply {
                        eraseColor(viewModel.tableConfig.background.removePrefix("#").toInt())
                    }
                } catch (e: Exception) {
                    Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
                        eraseColor(Color.GRAY)
                    }
                }
                Glide.with(this)
                        .load(bitmap)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(bg)
            } else {
                val p = ViewUtils.getRealSize(requireActivity())
                val x = (p.x * 0.5).toInt()
                val y = (p.y * 0.5).toInt()
                Glide.with(this)
                        .load(viewModel.tableConfig.background)
                        .override(x, y)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .error(BitmapDrawable(resources, Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
                            eraseColor(Color.GRAY)
                        }))
                        .into(bg)
            }
        } else {
            val x = (ViewUtils.getRealSize(requireActivity()).x * 0.5).toInt()
            val y = (ViewUtils.getRealSize(requireActivity()).y * 0.5).toInt()
            Glide.with(this)
                    .load(R.drawable.main_background_2020_1)
                    .override(x, y)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(bg)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val v = (requireView() as ConstraintLayout)
        v.removeAllViews()
        layout(v)
        launch { loadSchedule() }
        loadBg()
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
                "格子高度" -> viewModel.tableConfig.itemHeight = value
                "格子圆角半径" -> viewModel.tableConfig.radius = value
                "格子不透明度" -> viewModel.tableConfig.itemAlpha = value
                "格子文字大小" -> viewModel.tableConfig.itemTextSize = value
            }
            item.valueInt = value
            mAdapter.notifyItemChanged(position)
            launch { loadSchedule() }
            dialog.dismiss()
        }
    }

    private fun onSwitchItemCheckChange(item: SwitchItem, isChecked: Boolean, position: Int) {
        when (item.title) {
            "显示周六" -> viewModel.tableConfig.showSat = isChecked
            "显示周日" -> viewModel.tableConfig.showSun = isChecked
            "在格子内显示上课时间" -> viewModel.tableConfig.showTime = isChecked
            "在格子内显示授课老师" -> viewModel.tableConfig.showTeacher = isChecked
            "显示非本周课程" -> viewModel.tableConfig.showOtherWeekCourse = isChecked
            "格子文字水平居中" -> viewModel.tableConfig.itemCenterHorizontal = isChecked
            "格子文字竖直居中" -> viewModel.tableConfig.itemCenterVertical = isChecked
        }
        item.checked = isChecked
        launch { loadSchedule() }
    }

    private fun onVerticalItemClick(item: VerticalItem) {
        when (item.title) {
            "课程表背景" -> {
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle("设置背景类型")
                        .setItems(arrayOf("图片背景", "纯色背景")) { _, which ->
                            if (which == 0) {
                                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                    type = "image/*"
                                }
                                try {
                                    startActivityForResult(intent, Const.REQUEST_CODE_CHOOSE_BG)
                                } catch (e: ActivityNotFoundException) {
                                    e.printStackTrace()
                                }
                            } else {
                                val color = if (viewModel.tableConfig.background.startsWith("#")) {
                                    viewModel.tableConfig.background.removePrefix("#").toInt()
                                } else {
                                    Color.GRAY
                                }
                                buildColorPickerDialogBuilder(requireActivity(), color, BG_COLOR, false)
                            }
                        }
                        .show()
            }
            "界面文字颜色" -> {
                buildColorPickerDialogBuilder(requireActivity(), viewModel.tableConfig.textColor, TITLE_COLOR)
            }
            "课程文字颜色" -> {
                buildColorPickerDialogBuilder(requireActivity(), viewModel.tableConfig.courseTextColor, COURSE_TEXT_COLOR)
            }
            "格子边框颜色" -> {
                buildColorPickerDialogBuilder(requireActivity(), viewModel.tableConfig.strokeColor, STROKE_COLOR)
            }
        }
    }

    private fun onVerticalItemLongClick(item: VerticalItem): Boolean {
        return when (item.title) {
            "课程表背景" -> {
                viewModel.tableConfig.background = ""
                loadBg()
                Toasty.success(requireContext(), "恢复默认壁纸成功~").show()
                true
            }
            else -> false
        }
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
            TITLE_COLOR -> viewModel.tableConfig.textColor = newColor
            COURSE_TEXT_COLOR -> viewModel.tableConfig.courseTextColor = newColor
            STROKE_COLOR -> viewModel.tableConfig.strokeColor = newColor
            BG_COLOR -> {
                viewModel.tableConfig.background = "#${newColor}"
                loadBg()
            }
        }
        launch { loadSchedule() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Const.REQUEST_CODE_CHOOSE_BG && resultCode == AppCompatActivity.RESULT_OK) {
            //viewModel.table.background = Matisse.obtainResult(data)[0].toString()
            val uri = data?.data
            if (uri != null) {
                launch {
                    var bufferedSource: BufferedSource? = null
                    var bufferedSink: BufferedSink? = null
                    val path = withContext(Dispatchers.IO) {
                        try {
                            val inputStream = requireContext().contentResolver.openInputStream(uri)
                            bufferedSource = Okio.buffer(Okio.source(inputStream))
                            val out = File(requireContext().filesDir, "table${viewModel.table.id}_bg_${System.currentTimeMillis()}")
                            bufferedSink = Okio.buffer(Okio.sink(out))
                            bufferedSink?.writeAll(bufferedSource)
                            bufferedSink?.close()
                            bufferedSource?.close()
                            out.path
                        } catch (e: Exception) {
                            bufferedSink?.close()
                            bufferedSource?.close()
                            null
                        }
                    }
                    if (path != null) {
                        viewModel.tableConfig.background = path
                        loadBg()
                    } else {
                        Toasty.error(requireContext(), "图片读取失败>_<").show()
                    }
                }
            }
        }
    }

}