package com.suda.yzune.wakeupschedule.schedule_manage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseFragment
import com.suda.yzune.wakeupschedule.bean.TableConfig
import com.suda.yzune.wakeupschedule.schedule_settings.ScheduleSettingsActivity
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.ViewUtils
import com.suda.yzune.wakeupschedule.utils.getPrefer
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_list_manage.*
import splitties.activities.start
import splitties.dimensions.dip
import splitties.resources.dimenPxSize

class ScheduleManageFragment : BaseFragment() {

    private val viewModel by activityViewModels<ScheduleManageViewModel>()
    private lateinit var adapter: TableListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_list_manage, container, false)
        val rvTableList = view.findViewById<RecyclerView>(R.id.rv_list)
        launch {
            initTableRecyclerView(view, rvTableList, viewModel.initTableSelectList())
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (requireContext().getPrefer().getBoolean(Const.KEY_HIDE_NAV_BAR, false)) {
            ViewCompat.setOnApplyWindowInsetsListener(fab_add) { v, insets ->
                v.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    bottomMargin = insets.systemWindowInsets.bottom + v.dip(16)
                }
                insets
            }
        }
        fab_add.setOnClickListener {
            val dialog = MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.setting_schedule_name)
                    .setView(R.layout.dialog_edit_text)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.sure, null)
                    .create()
            dialog.show()
            val inputLayout = dialog.findViewById<TextInputLayout>(R.id.text_input_layout)
            val editText = dialog.findViewById<TextInputEditText>(R.id.edit_text)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val value = editText?.text
                if (value.isNullOrBlank()) {
                    inputLayout?.error = "名称不能为空哦>_<"
                } else {
                    launch {
                        try {
                            val tableName = editText.text.toString()
                            adapter.addData(viewModel.addBlankTable(tableName))
                            Toasty.success(requireContext(), "新建成功~").show()
                        } catch (e: Exception) {
                            Toasty.error(requireContext(), "操作失败>_<").show()
                        }
                        dialog.dismiss()
                    }
                }
            }
        }
    }

    private fun initTableRecyclerView(fragmentView: View, rvTableList: RecyclerView, data: MutableList<TableConfig>) {
        if (ViewUtils.getScreenInfo(requireContext())[0] < dimenPxSize(R.dimen.wide_screen)) {
            rvTableList.layoutManager = LinearLayoutManager(context)
        } else {
            rvTableList.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
        }
        adapter = TableListAdapter(R.layout.item_table_list, data)
        adapter.setOnItemClickListener { _, _, position ->
            val bundle = Bundle()
            bundle.putInt("selectedTableId", data[position].id)
            Navigation.findNavController(fragmentView).navigate(R.id.scheduleManageFragment_to_courseManageFragment, bundle)
        }
        adapter.addChildClickViewIds(R.id.ib_share, R.id.ib_edit, R.id.ib_delete)
        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.ib_share -> {
                }
                R.id.ib_edit -> {
                    launch {
                        val task = viewModel.getTableById(data[position].id)
                        if (task != null) {
                            requireActivity().start<ScheduleSettingsActivity> {
                                putExtra("tableData", task)
                            }
                        } else {
                            Toasty.error(requireContext(), "读取课表异常>_<")
                        }
                    }
                }
                R.id.ib_delete -> {
                    Toasty.info(requireContext(), "长按删除课程表哦~").show()
                }
            }
        }
        adapter.addChildLongClickViewIds(R.id.ib_delete)
        adapter.setOnItemChildLongClickListener { _, view, position ->
            when (view.id) {
                R.id.ib_delete -> {
                    launch {
                        viewModel.deleteTable(data[position].id)
                        adapter.removeAt(position)
                        Toasty.success(requireContext(), "删除成功~").show()
                    }
                    return@setOnItemChildLongClickListener true
                }
                else -> {
                    return@setOnItemChildLongClickListener false
                }
            }

        }
        adapter.addFooterView(View(activity).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dip(240))
        })
        adapter.addHeaderView(AppCompatTextView(requireContext()).apply {
            text = "点击卡片查看该课表的课程"
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setPadding(0, dip(8), 0, dip(8))
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        })
        rvTableList.adapter = adapter
    }

}
