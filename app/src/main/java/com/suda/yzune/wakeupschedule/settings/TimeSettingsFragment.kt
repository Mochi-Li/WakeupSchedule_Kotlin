package com.suda.yzune.wakeupschedule.settings

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseFragment
import com.suda.yzune.wakeupschedule.utils.CourseUtils
import es.dmoral.toasty.Toasty
import splitties.dimensions.dip

class TimeSettingsFragment : BaseFragment() {

    var position = 0
    private val viewModel by activityViewModels<TimeSettingsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = requireArguments().getInt("position")
        if (viewModel.timeSelectList.isEmpty()) {
            viewModel.initTimeSelectList()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_time_settings, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_time_detail)
        initAdapter(recyclerView)
        viewModel.getTimeData(viewModel.timeTableList[position].id).observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            if (it.isEmpty()) {
                launch {
                    viewModel.initTimeTableData(viewModel.timeTableList[position].id)
                }
            } else {
                viewModel.timeList.clear()
                viewModel.timeList.addAll(it)
                recyclerView.adapter?.notifyDataSetChanged()
            }
        })
        return view
    }

    private fun initAdapter(recyclerView: RecyclerView) {
        val adapter = TimeSettingsAdapter(R.layout.item_time_detail, viewModel.timeList)
        adapter.setOnItemClickListener { _, v, position ->
            val t = viewModel.timeList[position].startTime.split(':').map {
                try {
                    it.toInt()
                } catch (e: Exception) {
                    0
                }
            }
            Toasty.info(requireContext(), "现在设置第 ${position + 1} 节上课时间").show()
            TimePickerDialog(context, { _, startHour, startMinute ->
                val h = if (startHour < 10) "0$startHour" else startHour.toString()
                val m = if (startMinute < 10) "0$startMinute" else startMinute.toString()
                val start = "$h:$m"
                viewModel.timeList[position].startTime = start
                if (viewModel.timeTableList[this.position].sameLen) {
                    val end = CourseUtils.calAfterTime(start, viewModel.timeTableList[this.position].courseLen)
                    viewModel.timeList[position].endTime = end
                    v.findViewById<TextView>(R.id.tv_start).text = start
                    v.findViewById<TextView>(R.id.tv_end).text = end
                } else {
                    val endT = viewModel.timeList[position].endTime.split(':').map {
                        try {
                            it.toInt()
                        } catch (e: Exception) {
                            0
                        }
                    }
                    Toasty.info(requireContext(), "现在设置第 ${position + 1} 节下课时间").show()
                    TimePickerDialog(context, { _, endHour, endMinute ->
                        val endH = if (endHour < 10) "0$endHour" else endHour.toString()
                        val endM = if (endMinute < 10) "0$endMinute" else endMinute.toString()
                        val end = "$endH:$endM"
                        viewModel.timeList[position].endTime = end
                        v.findViewById<TextView>(R.id.tv_start).text = start
                        v.findViewById<TextView>(R.id.tv_end).text = end
                    }, endT[0], endT[1], true).apply {
                        // setMessage("设置第 ${position + 1} 节下课时间")
                        setCancelable(false)
                        show()
                    }
                }
            }, t[0], t[1], true).apply {
                // setMessage("设置第 ${position + 1} 节上课时间")
                setCancelable(false)
                show()
            }
        }
        adapter.setHeaderView(initHeaderView(adapter))
        adapter.addFooterView(View(activity).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dip(240))
        })
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
    }

    private fun initHeaderView(adapter: TimeSettingsAdapter): View {
        val view = LayoutInflater.from(activity).inflate(R.layout.item_time_detail_header, null)
        val llLength = view.findViewById<LinearLayoutCompat>(R.id.ll_set_length)
        val switch = view.findViewById<SwitchCompat>(R.id.s_time_same)
        if (viewModel.timeTableList[position].sameLen) {
            llLength.visibility = View.VISIBLE
        } else {
            llLength.visibility = View.GONE
        }
        switch.isChecked = viewModel.timeTableList[position].sameLen
        switch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.timeTableList[position].sameLen = isChecked
            if (isChecked) {
                llLength.visibility = View.VISIBLE
            } else {
                llLength.visibility = View.GONE
            }
        }

        val tvTimeLen = view.findViewById<AppCompatTextView>(R.id.tv_time_length)
        val slider = view.findViewById<Slider>(R.id.sb_time_length)
        val editBtn = view.findViewById<View>(R.id.ib_edit)
        slider.value = viewModel.timeTableList[position].courseLen.toFloat()
        tvTimeLen.text = viewModel.timeTableList[position].courseLen.toString()
        slider.addOnChangeListener { _, value, _ ->
            tvTimeLen.text = value.toInt().toString()
        }
        slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {

            }

            override fun onStopTrackingTouch(slider: Slider) {
                val value = slider.value.toInt()
                viewModel.refreshEndTime(value)
                viewModel.timeTableList[position].courseLen = value
                adapter.notifyDataSetChanged()
            }

        })

        val maxVal = 120
        val minVal = 10
        editBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val dialog = MaterialAlertDialogBuilder(requireContext())
                        .setTitle("课程时长")
                        .setView(R.layout.dialog_edit_text)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, null)
                        .setCancelable(false)
                        .create()
                dialog.show()
                val inputLayout = dialog.findViewById<TextInputLayout>(R.id.text_input_layout)
                val editText = dialog.findViewById<TextInputEditText>(R.id.edit_text)
                inputLayout?.helperText = "范围 $minVal ~ $maxVal"
                inputLayout?.suffixText = "分钟"
                editText?.inputType = InputType.TYPE_CLASS_NUMBER
                if (viewModel.timeTableList[position].courseLen < minVal) {
                    viewModel.timeTableList[position].courseLen = minVal
                }
                if (viewModel.timeTableList[position].courseLen > maxVal) {
                    viewModel.timeTableList[position].courseLen = maxVal
                }
                val valueStr = viewModel.timeTableList[position].courseLen.toString()
                editText?.setText(valueStr)
                editText?.setSelection(valueStr.length)
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val value = editText?.text
                    if (value.isNullOrBlank()) {
                        inputLayout?.error = "数值不能为空哦>_<"
                        return@setOnClickListener
                    }
                    val valueInt = try {
                        value.toString().toInt()
                    } catch (e: Exception) {
                        inputLayout?.error = "输入异常>_<"
                        return@setOnClickListener
                    }
                    if (valueInt < minVal || valueInt > maxVal) {
                        inputLayout?.error = "注意范围 $minVal ~ $maxVal"
                        return@setOnClickListener
                    }
                    viewModel.timeTableList[position].courseLen = valueInt
                    slider.value = valueInt.toFloat()
                    viewModel.refreshEndTime(valueInt)
                    adapter.notifyDataSetChanged()
                    dialog.dismiss()
                }
            }
        })

        val tvName = view.findViewById<AppCompatTextView>(R.id.tv_table_name)
        val llName = view.findViewById<LinearLayoutCompat>(R.id.ll_table_name)
        tvName.text = viewModel.timeTableList[position].name
        llName.setOnClickListener {
            if (viewModel.timeTableList[position].id == 1) {
                Toasty.error(requireContext(), "默认时间表不能改名呢>_<").show()
                return@setOnClickListener
            }
            val dialog = MaterialAlertDialogBuilder(requireContext())
                    .setTitle("时间表名字")
                    .setView(R.layout.dialog_edit_text)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.ok, null)
                    .create()
            dialog.show()
            val inputLayout = dialog.findViewById<TextInputLayout>(R.id.text_input_layout)
            val editText = dialog.findViewById<TextInputEditText>(R.id.edit_text)
            editText?.setText(viewModel.timeTableList[position].name)
            editText?.setSelection(viewModel.timeTableList[position].name.length)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val value = editText?.text
                if (value.isNullOrBlank()) {
                    inputLayout?.error = "名称不能为空哦>_<"
                } else {
                    tvName.text = editText.text.toString()
                    viewModel.timeTableList[position].name = editText.text.toString()
                    dialog.dismiss()
                }
            }
        }
        return view
    }
}
