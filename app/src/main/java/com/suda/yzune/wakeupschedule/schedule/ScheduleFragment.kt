package com.suda.yzune.wakeupschedule.schedule

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseFragment
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.CourseUtils
import com.suda.yzune.wakeupschedule.utils.getPrefer
import splitties.dimensions.dip

class ScheduleFragment : BaseFragment() {

    private var week = 0
    private var preLoad = true
    private var weekDay = 1
    private lateinit var weekDate: List<String>
    private val viewModel by activityViewModels<ScheduleViewModel>()
    private lateinit var ui: ScheduleUI
    private var isLoaded = false
    private lateinit var showCourseNumber: LiveData<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            week = it.getInt("week")
            preLoad = it.getBoolean("preLoad")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        weekDay = CourseUtils.getWeekdayInt()
        ui = ScheduleUI(requireContext(), viewModel.tableConfig, viewModel.tableConfig, if (week == viewModel.currentWeek) weekDay else -1)
        showCourseNumber = viewModel.getShowCourseNumber(week)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        weekDate = CourseUtils.getDateStringFromWeek(CourseUtils.countWeek(viewModel.tableConfig.startDate, viewModel.tableConfig.sundayFirst), week, viewModel.tableConfig.sundayFirst)
        ((view as ConstraintLayout).getViewById(R.id.anko_tv_title0) as TextView).text = weekDate[0] + "\n月"
        var textView: TextView?
        for (i in 1..7) {
            if (ui.dayMap[i] == -1) continue
            textView = view.getViewById(R.id.anko_tv_title0 + ui.dayMap[i]) as TextView
            if (i == 7 && !viewModel.tableConfig.showSat && !viewModel.tableConfig.sundayFirst) {
                textView.text = viewModel.daysArray[i] + "\n${weekDate[7]}"
            } else if (!viewModel.tableConfig.showSun && viewModel.tableConfig.sundayFirst && i != 7) {
                textView.text = viewModel.daysArray[i] + "\n${weekDate[ui.dayMap[i] + 1]}"
            } else {
                textView.text = viewModel.daysArray[i] + "\n${weekDate[ui.dayMap[i]]}"
            }
        }
        if (viewModel.timeList.isNotEmpty() && ui.showTimeDetail) {
            for (i in 0 until viewModel.tableConfig.nodes) {
                (ui.content.getViewById(R.id.anko_tv_node1 + i) as LinearLayout).apply {
                    findViewById<TextView>(R.id.tv_start).text = viewModel.timeList[i].startTime
                    findViewById<TextView>(R.id.tv_end).text = viewModel.timeList[i].endTime
                }
            }
        }
        if (preLoad) {
            for (i in 1..7) {
                viewModel.allCourseList[i - 1].observe(viewLifecycleOwner, Observer {
                    ui.initWeekPanel(it, viewModel.timeList, week, i, requireActivity())
                })
            }
        }
        showCourseNumber.observe(viewLifecycleOwner, Observer {
            if (it == 0) {
                ui.content.visibility = View.GONE
                if (ui.root.getViewById(R.id.anko_empty_view) != null) {
                    return@Observer
                }
                val img = ImageView(requireContext()).apply {
                    setImageResource(R.drawable.ic_schedule_empty)
                }
                ui.root.addView(LinearLayout(requireContext()).apply {
                    id = R.id.anko_empty_view
                    orientation = LinearLayout.VERTICAL
                    if (context.getPrefer().getBoolean(Const.KEY_SHOW_EMPTY_VIEW, true)) {
                        addView(img, LinearLayout.LayoutParams.WRAP_CONTENT, dip(240))
                    }
                    addView(TextView(context).apply {
                        text = "本周没有课程哦"
                        setTextColor(viewModel.tableConfig.textColor)
                        gravity = Gravity.CENTER
                    }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                        topMargin = dip(16)
                    })
                }, ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT).apply {
                    startToStart = ConstraintSet.PARENT_ID
                    endToEnd = ConstraintSet.PARENT_ID
                    topToBottom = R.id.anko_tv_title0
                    bottomToBottom = ConstraintSet.PARENT_ID
                    marginStart = requireContext().dip(32)
                    marginEnd = requireContext().dip(32)
                })
            } else {
                ui.content.visibility = View.VISIBLE
                ui.root.getViewById(R.id.anko_empty_view)?.let { emptyView ->
                    ui.root.removeView(emptyView)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (preLoad) return
        if (!isLoaded) {
            for (i in 1..7) {
                viewModel.allCourseList[i - 1].observe(viewLifecycleOwner, Observer {
                    ui.initWeekPanel(it, viewModel.timeList, week, i, requireActivity())
                })
            }
            isLoaded = true
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(week: Int, preLoad: Boolean) =
                ScheduleFragment().apply {
                    arguments = Bundle().apply {
                        putInt("week", week)
                        putBoolean("preLoad", preLoad)
                    }
                }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isLoaded = false
    }

}