package com.suda.yzune.wakeupschedule.course_add

import android.app.Activity
import android.app.Dialog
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.setMargins
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseListActivity
import com.suda.yzune.wakeupschedule.bean.CourseBaseBean
import com.suda.yzune.wakeupschedule.bean.CourseEditBean
import com.suda.yzune.wakeupschedule.schedule_import.Common
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.CourseUtils
import com.suda.yzune.wakeupschedule.utils.ViewUtils
import com.suda.yzune.wakeupschedule.utils.getPrefer
import com.suda.yzune.wakeupschedule.widget.EditDetailFragment
import com.suda.yzune.wakeupschedule.widget.colorpicker.ColorPickerFragment
import es.dmoral.toasty.Toasty
import splitties.dimensions.dip
import splitties.resources.color
import splitties.resources.styledColor

class AddCourseActivity : BaseListActivity(), ColorPickerFragment.ColorPickerDialogListener, AddCourseAdapter.OnItemEditTextChangedListener {

    private lateinit var tvColor: AppCompatTextView
    private lateinit var ivColor: AppCompatImageView

    override fun onSetupSubButton(): View? {
        val tvButton = AppCompatTextView(this)
        tvButton.text = "保存"
        tvButton.typeface = Typeface.DEFAULT_BOLD
        tvButton.setTextColor(color(R.color.colorAccent))
        tvButton.setOnClickListener {
            saveAndExit()
        }
        return tvButton
    }

    private val viewModel by viewModels<AddCourseViewModel>()
    private lateinit var etName: AppCompatEditText
    private lateinit var adapter: AddCourseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.extras!!.getInt("id") == -1) {
            viewModel.tableId = intent.extras!!.getInt("tableId")
            viewModel.maxWeek = intent.extras!!.getInt("maxWeek")
            viewModel.nodes = intent.extras!!.getInt("nodes")
            adapter = AddCourseAdapter(R.layout.item_add_course_detail, viewModel.initData(viewModel.maxWeek))
            initAdapter(viewModel.baseBean)
        } else {
            viewModel.tableId = intent.extras!!.getInt("tableId")
            viewModel.maxWeek = intent.extras!!.getInt("maxWeek")
            viewModel.nodes = intent.extras!!.getInt("nodes")
            adapter = AddCourseAdapter(R.layout.item_add_course_detail, viewModel.editList)
            launch {
                val detailList = viewModel.initData(intent.extras!!.getInt("id"), viewModel.tableId)
                detailList.forEach {
                    viewModel.editList.add(CourseUtils.detailBean2EditBean(it))
                }
                adapter.notifyDataSetChanged()
                val courseBaseBean = viewModel.initBaseData(intent.extras!!.getInt("id"))
                viewModel.baseBean.id = courseBaseBean.id
                viewModel.baseBean.color = courseBaseBean.color
                viewModel.baseBean.courseName = courseBaseBean.courseName
                viewModel.baseBean.tableId = courseBaseBean.tableId
                initAdapter(viewModel.baseBean)
            }
        }
        val addFab = FloatingActionButton(this).apply {
            setImageResource(R.drawable.ic_outline_add_24)
            imageTintList = ViewUtils.createColorStateList(styledColor(R.attr.colorSurface))
            setOnClickListener {
                if (viewModel.editList.isEmpty()) {
                    adapter.addData(CourseEditBean(
                            teacher = "",
                            room = "",
                            tableId = viewModel.tableId,
                            weekList = MutableLiveData<ArrayList<Int>>().apply {
                                this.value = ArrayList<Int>().apply {
                                    for (i in 1..viewModel.maxWeek) {
                                        this.add(i)
                                    }
                                }
                            }))
                } else {
                    adapter.addData(CourseEditBean(
                            teacher = viewModel.editList[0].teacher,
                            room = viewModel.editList[0].room,
                            tableId = viewModel.tableId,
                            weekList = MutableLiveData<ArrayList<Int>>().apply {
                                this.value = ArrayList<Int>().apply {
                                    for (i in 1..viewModel.maxWeek) {
                                        this.add(i)
                                    }
                                }
                            }))
                }
                (mRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(adapter.data.size, 0)
            }
        }
        rootView.addView(addFab, ConstraintLayout.LayoutParams(dip(56), dip(67)).apply {
            bottomToBottom = ConstraintSet.PARENT_ID
            endToEnd = ConstraintSet.PARENT_ID
            setMargins(dip(16))
        })
        if (getPrefer().getBoolean(Const.KEY_HIDE_NAV_BAR, false)) {
            ViewCompat.setOnApplyWindowInsetsListener(addFab) { v, insets ->
                v.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    bottomMargin = insets.systemWindowInsets.bottom + v.dip(16)
                }
                if (!adapter.hasFooterLayout()) {
                    adapter.addFooterView(View(this).apply {
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, insets.systemWindowInsets.bottom)
                    })
                }
                insets
            }
        }
    }

    override fun onEditTextAfterTextChanged(editable: Editable, position: Int, what: String) {
        when (what) {
            "room" -> viewModel.editList[position].room = editable.toString()
            "teacher" -> viewModel.editList[position].teacher = editable.toString()
        }
    }

    private fun saveAndExit() {
        if (viewModel.baseBean.courseName == "") {
            Toasty.error(this, "请填写课程名称").show()
        } else {
            if (viewModel.baseBean.id == -1 || !viewModel.updateFlag) {
                launch {
                    val task = viewModel.checkSameName()
                    if (task != null) {
                        viewModel.baseBean.id = task.id
                    }
                    saveData(task != null)
                }
            } else {
                saveData()
            }
        }
    }

    private fun initAdapter(baseBean: CourseBaseBean) {
        adapter.setListener(this)
        adapter.addHeaderView(initHeaderView(baseBean))
        adapter.addChildClickViewIds(R.id.ll_time, R.id.ib_delete,
                R.id.ll_weeks, R.id.ll_teacher, R.id.ll_room)
        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.ll_time -> {
                    viewModel.editList[position].time.observe(this, Observer {
                        val textView = adapter.getViewByPosition(position + 1, R.id.et_time) as AppCompatTextView
                        textView.text = "${CourseUtils.getDayStr(it!!.day)}    第${it.startNode} - ${it.endNode}节"
                    })
                    val selectTimeDialog = SelectTimeFragment.newInstance(position)
                    selectTimeDialog.isCancelable = false
                    selectTimeDialog.show(supportFragmentManager, "selectTime")
                }
                R.id.ib_delete -> {
                    if (adapter.data.size == 1) {
                        Toasty.error(this, "至少要保留一个时间段").show()
                    } else {
                        adapter.remove(position)
                    }
                }
                R.id.ll_weeks -> {
                    viewModel.editList[position].weekList.observe(this, Observer {
                        it!!.sort()
                        val textView = adapter.getViewByPosition(position + 1, R.id.et_weeks) as AppCompatTextView
                        val text = Common.weekIntList2WeekBeanList(it).toString()
                        textView.text = text.substring(1, text.length - 1)
                    })
                    val selectWeekDialog = SelectWeekFragment.newInstance(position)
                    selectWeekDialog.isCancelable = false
                    selectWeekDialog.show(supportFragmentManager, "selectWeek")
                }
                R.id.ll_teacher -> {
                    launch {
                        val textView = adapter.getViewByPosition(position + 1, R.id.et_teacher) as AppCompatTextView
                        if (viewModel.teacherList == null) {
                            viewModel.teacherList = viewModel.getExistedTeachers()
                        }
                        EditDetailFragment.newInstance("授课老师", viewModel.teacherList!!, viewModel.editList[position].teacher
                                ?: "").apply {
                            listener = object : EditDetailFragment.OnSaveClickedListener {
                                override fun save(editText: AppCompatEditText, dialog: Dialog) {
                                    val teacher = editText.text.toString()
                                    textView.text = teacher
                                    viewModel.editList[position].teacher = teacher
                                    val flag = viewModel.teacherList!!.any {
                                        it == teacher
                                    }
                                    if (!flag) {
                                        viewModel.teacherList!!.add(teacher)
                                    }
                                    dialog.dismiss()
                                }
                            }
                        }.show(supportFragmentManager, null)
                    }
                }
                R.id.ll_room -> {
                    launch {
                        val textView = adapter.getViewByPosition(position + 1, R.id.et_room) as AppCompatTextView
                        if (viewModel.roomList == null) {
                            viewModel.roomList = viewModel.getExistedRooms()
                        }
                        EditDetailFragment.newInstance("上课地点", viewModel.roomList!!, viewModel.editList[position].room
                                ?: "").apply {
                            listener = object : EditDetailFragment.OnSaveClickedListener {
                                override fun save(editText: AppCompatEditText, dialog: Dialog) {
                                    val room = editText.text.toString()
                                    textView.text = room
                                    viewModel.editList[position].room = room
                                    val flag = viewModel.roomList!!.any {
                                        it == room
                                    }
                                    if (!flag) {
                                        viewModel.roomList!!.add(room)
                                    }
                                    dialog.dismiss()
                                }
                            }
                        }.show(supportFragmentManager, null)
                    }
                }
            }
        }
        mRecyclerView.adapter = adapter
        mRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun initHeaderView(baseBean: CourseBaseBean): View {
        val view = LayoutInflater.from(this).inflate(R.layout.item_add_course_base, null)
        etName = view.findViewById(R.id.et_name)
        val llColor = view.findViewById<LinearLayoutCompat>(R.id.ll_color)
        tvColor = view.findViewById(R.id.tv_color)
        ivColor = view.findViewById(R.id.iv_color)
        etName.setText(baseBean.courseName)
        etName.setSelection(baseBean.courseName.length)
        etName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                baseBean.courseName = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
        tvColor.text = baseBean.color
        if (baseBean.color != "") {
            val colorInt = Color.parseColor(baseBean.color)
            ivColor.imageTintList = ViewUtils.createColorStateList(colorInt)
            tvColor.setTextColor(colorInt)
            tvColor.text = "点此更改颜色"
        }
        llColor.setOnClickListener {
            ColorPickerFragment.newBuilder()
                    .setColor(if (baseBean.color != "") tvColor.textColors.defaultColor else color(R.color.red))
                    .setShowAlphaSlider(false)
                    .show(this)
        }
        return view
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        tvColor.setTextColor(color)
        tvColor.text = "点此更改颜色"
        ivColor.imageTintList = ViewUtils.createColorStateList(color)
        viewModel.baseBean.color = "#${Integer.toHexString(color)}"
    }

    private fun saveData(isSame: Boolean = false) {
        launch {
            val maxId = viewModel.getLastId()
            if (viewModel.newId == -1) {
                if (maxId == null) {
                    viewModel.newId = 0
                } else {
                    viewModel.newId = maxId + 1
                }
            }

            try {
                viewModel.preSaveData(isSame)
                val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
                val list = viewModel.getScheduleWidgetIds()
                list.forEach {
                    when (it.detailType) {
                        0 -> appWidgetManager.notifyAppWidgetViewDataChanged(it.id, R.id.lv_schedule)
                        1 -> appWidgetManager.notifyAppWidgetViewDataChanged(it.id, R.id.lv_course)
                    }
                }
                Toasty.success(this@AddCourseActivity, "保存成功").show()
                if (!viewModel.updateFlag) {
                    setResult(Activity.RESULT_OK, Intent().putExtra("course", viewModel.baseBean))
                }
                finish()
            } catch (e: Exception) {
                Toasty.error(this@AddCourseActivity, e.message ?: "发生异常", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onBackPressed() {
        val builder = MaterialAlertDialogBuilder(this)
                .setMessage("需要保存当前的编辑吗？")
                .setNegativeButton("离开") { _, _ ->
                    finish()
                }
                .setPositiveButton("留下", null)
        if (viewModel.baseBean.courseName.isNotEmpty()) {
            builder.setPositiveButton("保存") { _, _ ->
                saveAndExit()
            }
        }
        builder.show()
    }
}
