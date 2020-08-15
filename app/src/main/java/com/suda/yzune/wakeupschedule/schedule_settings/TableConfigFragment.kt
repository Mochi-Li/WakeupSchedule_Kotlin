package com.suda.yzune.wakeupschedule.schedule_settings

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseListFragment
import com.suda.yzune.wakeupschedule.schedule_manage.ScheduleManageActivity
import com.suda.yzune.wakeupschedule.settings.SettingItemAdapter
import com.suda.yzune.wakeupschedule.settings.TimeSettingsActivity
import com.suda.yzune.wakeupschedule.settings.items.*
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.Utils
import es.dmoral.toasty.Toasty
import splitties.activities.start
import splitties.dimensions.dip

class TableConfigFragment : BaseListFragment() {

    private val mAdapter = SettingItemAdapter()
    private val viewModel by activityViewModels<ScheduleSettingsViewModel>()

    private val currentWeekItem by lazy(LazyThreadSafetyMode.NONE) {
        SeekBarItem(R.string.setting_current_week, viewModel.getCurrentWeek(), 1, viewModel.tableConfig.maxWeek, "周", "第")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val items = mutableListOf<BaseSettingItem>()
        onItemsCreated(items)
        mAdapter.data = items
        mRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        mRecyclerView.itemAnimator?.changeDuration = 250
        mRecyclerView.adapter = mAdapter

        mAdapter.addChildClickViewIds(R.id.anko_check_box)
        mAdapter.setOnItemChildClickListener { _, itemView, position ->
            when (val item = items[position]) {
                is SwitchItem -> onSwitchItemCheckChange(item, itemView.findViewById<AppCompatCheckBox>(R.id.anko_check_box).isChecked, position)
            }
        }
        mAdapter.setOnItemClickListener { _, itemView, position ->
            when (val item = items[position]) {
                is HorizontalItem -> onHorizontalItemClick(item, position)
//                is VerticalItem -> onVerticalItemClick(item)
                is SwitchItem -> itemView.findViewById<AppCompatCheckBox>(R.id.anko_check_box).performClick()
                is SeekBarItem -> onSeekBarItemClick(item, position)
            }
        }
        viewModel.termStartList = viewModel.tableConfig.startDate.split("-")
        viewModel.mYear = Integer.parseInt(viewModel.termStartList[0])
        viewModel.mMonth = Integer.parseInt(viewModel.termStartList[1])
        viewModel.mDay = Integer.parseInt(viewModel.termStartList[2])
        val settingItem = arguments?.getInt("settingItem")
        if (settingItem != null && savedInstanceState == null) {
            mRecyclerView.postDelayed({
                try {
                    val i = items.indexOfFirst {
                        it.title == settingItem
                    }
                    (mRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(i, requireContext().dip(64))
                    when (items[i]) {
                        is HorizontalItem -> onHorizontalItemClick(items[i] as HorizontalItem, i)
                        is SeekBarItem -> onSeekBarItemClick(items[i] as SeekBarItem, i)
                    }
                } catch (e: Exception) {

                }
            }, 100)
        }
    }

    private fun onItemsCreated(items: MutableList<BaseSettingItem>) {
        items.add(HorizontalItem(R.string.setting_schedule_name, viewModel.tableConfig.tableName))
        items.add(HorizontalItem(R.string.setting_class_time, "点击此处更改"))
        items.add(HorizontalItem(R.string.setting_term_start_date, viewModel.tableConfig.startDate))
        items.add(currentWeekItem)
        items.add(SeekBarItem(R.string.setting_nodes, viewModel.tableConfig.nodes, 1, 30, "节"))
        items.add(SeekBarItem(R.string.setting_weeks, viewModel.tableConfig.maxWeek, 1, 60, "周"))
        items.add(SwitchItem(R.string.setting_sunday_first, viewModel.tableConfig.sundayFirst))
        items.add(HorizontalItem(R.string.setting_manage_course, ""))
        items.add(VerticalItem(R.string.setting_blank, "\n\n\n"))
//        items.add(SwitchItem("显示周六", viewModel.tableConfig.showSat))
//        items.add(SwitchItem("显示周日", viewModel.tableConfig.showSun))
    }

    private fun onSwitchItemCheckChange(item: SwitchItem, isChecked: Boolean, position: Int) {
        when (item.title) {
            R.string.setting_sunday_first -> viewModel.tableConfig.sundayFirst = isChecked
        }
        item.checked = isChecked
    }

    private fun onSeekBarItemClick(item: SeekBarItem, position: Int) {
        Utils.showSeekBarItemDialog(requireContext(), item) { dialog, valueInt ->
            when (item.title) {
                R.string.setting_nodes -> viewModel.tableConfig.nodes = valueInt
                R.string.setting_weeks -> {
                    currentWeekItem.max = valueInt
                    viewModel.tableConfig.maxWeek = valueInt
                }
                R.string.setting_current_week -> {
                    viewModel.setCurrentWeek(valueInt)
                    item.valueInt = valueInt
                    (mAdapter.data[position - 1] as HorizontalItem).value = viewModel.tableConfig.startDate
                    mAdapter.notifyItemChanged(position - 1)
                    mAdapter.notifyItemChanged(position)
                    dialog.dismiss()
                }
            }
            item.valueInt = valueInt
            mAdapter.notifyItemChanged(position)
            dialog.dismiss()
        }
    }

    private fun onHorizontalItemClick(item: HorizontalItem, position: Int) {
        when (item.title) {
            R.string.setting_schedule_name -> {
                val dialog = MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.setting_schedule_name)
                        .setView(R.layout.dialog_edit_text)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, null)
                        .create()
                dialog.show()
                val inputLayout = dialog.findViewById<TextInputLayout>(R.id.text_input_layout)
                val editText = dialog.findViewById<TextInputEditText>(R.id.edit_text)
                editText?.setText(item.value)
                editText?.setSelection(item.value.length)
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val value = editText?.text
                    if (value.isNullOrBlank()) {
                        inputLayout?.error = "名称不能为空哦>_<"
                        return@setOnClickListener
                    }
                    viewModel.tableConfig.tableName = value.toString()
                    item.value = value.toString()
                    mAdapter.notifyItemChanged(position)
                    dialog.dismiss()
                }
            }
            R.string.setting_term_start_date -> {
                DatePickerDialog(requireContext(), DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    viewModel.mYear = year
                    viewModel.mMonth = monthOfYear + 1
                    viewModel.mDay = dayOfMonth
                    val mDate = "${viewModel.mYear}-${viewModel.mMonth}-${viewModel.mDay}"
                    item.value = mDate
                    viewModel.tableConfig.startDate = mDate
                    currentWeekItem.valueInt = viewModel.getCurrentWeek()
                    mAdapter.notifyItemChanged(position)
                    mAdapter.notifyItemChanged(position + 1)
                }, viewModel.mYear, viewModel.mMonth - 1, viewModel.mDay).show()
                if (viewModel.tableConfig.sundayFirst) {
                    Toasty.success(requireContext(), "为了周数计算准确，建议选择周日哦", Toasty.LENGTH_LONG).show()
                } else {
                    Toasty.success(requireContext(), "为了周数计算准确，建议选择周一哦", Toasty.LENGTH_LONG).show()
                }
            }
            R.string.setting_class_time -> {
                startActivityForResult(Intent(requireActivity(), TimeSettingsActivity::class.java).apply {
                    putExtra("tableData", viewModel.table)
                }, Const.REQUEST_CODE_CHOOSE_TABLE)
            }
            R.string.setting_manage_course -> {
                requireActivity().start<ScheduleManageActivity> {
                    putExtra("selectedTableId", viewModel.table.id)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            Const.REQUEST_CODE_CHOOSE_TABLE -> viewModel.table.timeTable = data!!.extras!!.getInt("timeTable")
        }
    }
}