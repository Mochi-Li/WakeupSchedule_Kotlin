package com.suda.yzune.wakeupschedule.schedule

import android.appwidget.AppWidgetManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.BaseDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.bean.CourseBean
import com.suda.yzune.wakeupschedule.course_add.AddCourseActivity
import com.suda.yzune.wakeupschedule.schedule_import.Common
import com.suda.yzune.wakeupschedule.utils.CourseUtils
import com.suda.yzune.wakeupschedule.utils.ViewUtils
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_course_detail.*
import kotlinx.android.synthetic.main.item_add_course_detail.*
import splitties.activities.start
import splitties.dimensions.dip
import splitties.resources.dimenPxSize
import splitties.snackbar.longSnack

class CourseDetailFragment : BaseDialogFragment() {

    override val layoutId: Int
        get() = R.layout.fragment_course_detail

    private lateinit var course: CourseBean
    private var nested: Boolean = false
    private var week = 0
    private val viewModel by activityViewModels<ScheduleViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            course = it.getParcelable<CourseBean>("course") as CourseBean
            nested = it.getBoolean("nested")
            week = it.getInt("week")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (!nested) {
            dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // dialog?.window?.setLayout(requireContext().dip(280), ViewGroup.LayoutParams.WRAP_CONTENT)
            val root = inflater.inflate(R.layout.fragment_base_dialog, container, false)
            val cardView = root.findViewById<MaterialCardView>(R.id.base_card_view)
            LayoutInflater.from(context).inflate(layoutId, cardView, true)
            return root
        } else {
            if (ViewUtils.getScreenInfo(requireContext())[0] >= requireContext().dimenPxSize(R.dimen.wide_screen)) {
                container!!.layoutParams.width = requireContext().dip(480)
            } else {
                container!!.layoutParams.width = requireContext().dip(280)
            }
            val root = inflater.inflate(R.layout.fragment_base_dialog, container, false)
            val cardView = root.findViewById<MaterialCardView>(R.id.base_card_view)
            cardView.setBackgroundColor(Color.TRANSPARENT)
            LayoutInflater.from(context).inflate(layoutId, cardView, true)
            cardView.findViewById<View>(R.id.include_detail).setBackgroundColor(Color.TRANSPARENT)
            return root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        showData()
        initEvent()
    }

    private fun initView() {
        tv_item.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
    }

    private fun showData() {
        tv_item.text = course.courseName
        et_teacher.text = course.teacher
        et_room.text = course.room
        val weekList = arrayListOf<Int>()
        viewModel.allCourseList[course.day - 1].value!!.filter {
            it.id == course.id && it.day == course.day && it.startNode == course.startNode
                    && it.step == course.step && it.teacher == course.teacher && it.room == course.room
        }.forEach {
            weekList.addAll(when (it.type) {
                0 -> {
                    (it.startWeek..it.endWeek).toList()
                }
                else -> {
                    (it.startWeek..it.endWeek step 2).toList()
                }
            })
        }
        et_weeks.text = Common.weekIntList2WeekBeanList(weekList).toString().removeSurrounding("[", "]")
        try {
            et_time.text = "第${course.startNode} - ${course.startNode + course.step - 1}节    ${viewModel.timeList[course.startNode - 1].startTime} - ${viewModel.timeList[course.startNode + course.step - 2].endTime}"
        } catch (e: Exception) {
            et_time.longSnack("该课程似乎有点问题哦>_<请修改一下")
        }
    }

    override fun dismiss() {
        if (nested) {
            (parentFragment as DialogFragment).dismiss()
        } else {
            super.dismiss()
        }
    }

    private fun initEvent() {
        ib_delete.setOnClickListener {
            dismiss()
        }

        ib_edit.setOnClickListener {
            dismiss()
            requireActivity().start<AddCourseActivity> {
                putExtra("id", course.id)
                putExtra("tableId", course.tableId)
                putExtra("maxWeek", viewModel.tableConfig.maxWeek)
                putExtra("nodes", viewModel.tableConfig.nodes)
            }
        }

        ib_delete_course.setOnClickListener {
            if (!course.inWeek(week)) {
                Toasty.info(requireContext(), "非本周课程请翻到含有这节课的周进行删除操作", Toasty.LENGTH_LONG).show()
                return@setOnClickListener
            }
            showDeleteDialog()
        }

    }

    private fun showDeleteDialog() {
        var index = 0
        val choices = arrayOf("仅第${week}周${CourseUtils.getDayStr(course.day)}的这节课",
                "全部${CourseUtils.getDayStr(course.day)}同老师同地点的这节课",
                "这门课程的全部时间段")
        MaterialAlertDialogBuilder(requireContext())
                .setTitle("选择删除范围，请三思😯")
                //.setMessage("此操作不可恢复哦")
                .setPositiveButton("确认删除") { _, _ ->
                    launch {
                        try {
                            when (index) {
                                0 -> viewModel.deleteCourseDetailThisWeek(course, week)
                                1 -> viewModel.deleteCourseDetailOfDayAllWeek(course)
                                2 -> viewModel.deleteCourseBaseBean(course.id, course.tableId)
                            }
                            Toasty.success(requireContext(), "删除成功").show()
                            val appWidgetManager = AppWidgetManager.getInstance(requireActivity().applicationContext)
                            val list = viewModel.getScheduleWidgetIds()
                            list.forEach {
                                when (it.detailType) {
                                    0 -> appWidgetManager.notifyAppWidgetViewDataChanged(it.id, R.id.lv_schedule)
                                    1 -> appWidgetManager.notifyAppWidgetViewDataChanged(it.id, R.id.lv_course)
                                }
                            }
                            dismiss()
                        } catch (e: Exception) {
                            Toasty.error(requireContext(), "出现异常>_<\n" + e.message).show()
                        }
                    }
                }
                .setSingleChoiceItems(choices, index) { _, which ->
                    index = which
                }
                .show()
    }

    companion object {
        @JvmStatic
        fun newInstance(week: Int, c: CourseBean, isNested: Boolean = false) =
                CourseDetailFragment().apply {
                    arguments = Bundle().apply {
                        putInt("week", week)
                        putParcelable("course", c)
                        putBoolean("nested", isNested)
                    }
                }
    }
}
