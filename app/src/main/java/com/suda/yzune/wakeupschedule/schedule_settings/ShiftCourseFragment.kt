package com.suda.yzune.wakeupschedule.schedule_settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.BaseDialogFragment
import androidx.fragment.app.activityViewModels
import com.suda.yzune.wakeupschedule.R
import kotlinx.android.synthetic.main.fragment_shift_course.*

class ShiftCourseFragment : BaseDialogFragment() {

    override val layoutId: Int
        get() = R.layout.fragment_shift_course

    private val viewModel by activityViewModels<ScheduleSettingsViewModel>()
    private val nodeList by lazy(LazyThreadSafetyMode.NONE) {
        (1 until viewModel.tableConfig.nodes).map { it.toString() }.toTypedArray()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wp_node.displayedValues = nodeList
        wp_direction.displayedValues = arrayOf("前", "后")
        wp_num.displayedValues = nodeList
        initEvent()
    }

    private fun initEvent() {
        wp_node.minValue = 0
        wp_node.maxValue = nodeList.size - 1

        wp_direction.minValue = 0
        wp_direction.maxValue = 1

        wp_num.minValue = 0
        wp_num.maxValue = nodeList.size - 1

        wp_node.setOnValueChangedListener { _, _, newVal ->

        }

        wp_direction.setOnValueChangedListener { _, _, newVal ->

        }

        wp_num.setOnValueChangedListener { _, _, newVal ->

        }

        btn_cancel.setOnClickListener {
            dismiss()
        }

        btn_save.setOnClickListener {

            dismiss()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
                ShiftCourseFragment()
    }
}
