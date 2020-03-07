package com.suda.yzune.wakeupschedule.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialAutoCompleteTextView
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseFragment
import com.suda.yzune.wakeupschedule.bean.TimeTableBean
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.getPrefer
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.time_table_fragment.*
import splitties.dimensions.dip
import splitties.snackbar.longSnack

class TimeTableFragment : BaseFragment() {

    private val viewModel by activityViewModels<TimeSettingsViewModel>()
    private lateinit var adapter: TimeTableAdapter
    private lateinit var arrayAdapter: ArrayAdapter<TimeTableBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arguments = arguments
        viewModel.selectedId = arguments!!.getInt("selectedId")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.time_table_fragment, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_time_table)
        launch {
            viewModel.timeTableList.clear()
            val list = viewModel.getTimeTableList()
            viewModel.timeTableList.addAll(list)
            initRecyclerView(recyclerView, view)
            arrayAdapter = ArrayAdapter(context!!, R.layout.popup_single_list_item)
            arrayAdapter.addAll(list)
            val index = list.indexOfFirst { it.id == viewModel.selectedId }
            view.findViewById<MaterialAutoCompleteTextView>(R.id.tv_time_table).apply {
                setText(list[index].name)
                setAdapter(arrayAdapter)
                listSelection = index
                setOnItemClickListener { _, _, position, _ ->
                    arrayAdapter.getItem(position)?.let {
                        viewModel.selectedId = it.id
                    }
                }
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (activity!!.getPrefer().getBoolean(Const.KEY_HIDE_NAV_BAR, false)) {
            ViewCompat.setOnApplyWindowInsetsListener(fab_add) { v, insets ->
                v.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    bottomMargin = insets.systemWindowInsets.bottom + v.dip(16)
                }
                insets
            }
        }
        fab_add.setOnClickListener {
            val dialog = MaterialAlertDialogBuilder(context!!)
                    .setTitle("时间表名字")
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
                            val id = viewModel.addNewTimeTable(value.toString())
                            adapter.addData(TimeTableBean(id, value.toString()))
                            arrayAdapter.add(TimeTableBean(id, value.toString()))
                            Toasty.success(activity!!, "新建成功~").show()
                        } catch (e: Exception) {
                            Toasty.error(activity!!, "发生异常>_<${e.message}").show()
                        }
                        dialog.dismiss()
                    }
                }
            }
        }
    }

    private fun initRecyclerView(recyclerView: RecyclerView, fragmentView: View) {
        adapter = TimeTableAdapter(R.layout.item_time_table, viewModel.timeTableList)
        recyclerView.adapter = adapter
        adapter.addFooterView(View(context!!).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dip(240))
        })
        adapter.addChildClickViewIds(R.id.ib_edit, R.id.ib_delete)
        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.ib_edit -> {
                    viewModel.entryPosition = position
                    val bundle = Bundle()
                    bundle.putInt("position", position)
                    Navigation.findNavController(fragmentView).navigate(R.id.timeTableFragment_to_timeSettingsFragment, bundle)
                }
                R.id.ib_delete -> {
                    Toasty.info(activity!!, "长按确认删除哦~").show()
                }
            }
        }
        adapter.addChildLongClickViewIds(R.id.ib_delete)
        adapter.setOnItemChildLongClickListener { _, view, position ->
            when (view.id) {
                R.id.ib_delete -> {
                    if (viewModel.timeTableList[position].id == viewModel.selectedId) {
                        view.longSnack("不能删除已选中的时间表哦>_<")
                    } else {
                        launch {
                            try {
                                viewModel.deleteTimeTable(viewModel.timeTableList[position])
                                adapter.remove(position)
                                arrayAdapter.remove(arrayAdapter.getItem(position))
                                view.longSnack("删除成功~")
                            } catch (e: Exception) {
                                view.longSnack("该时间表仍被使用中>_<请确保它不被使用再删除哦")
                            }
                        }
                    }
                    return@setOnItemChildLongClickListener true
                }
                else -> {
                    true
                }
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
    }

}
