package com.suda.yzune.wakeupschedule.schedule_manage

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseFragment
import com.suda.yzune.wakeupschedule.bean.CourseBaseBean
import com.suda.yzune.wakeupschedule.bean.TableConfig
import com.suda.yzune.wakeupschedule.course_add.AddCourseActivity
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.ViewUtils
import com.suda.yzune.wakeupschedule.utils.getPrefer
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_list_manage.*
import splitties.activities.start
import splitties.dimensions.dip
import splitties.resources.dimenPxSize

class CourseManageFragment : BaseFragment() {

    private val viewModel by activityViewModels<ScheduleManageViewModel>()
    private val table by lazy(LazyThreadSafetyMode.NONE) {
        val id = arguments?.getInt("selectedTableId") ?: return@lazy null
        TableConfig(requireContext(), id)
    }
    private lateinit var adapter: CourseListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_list_manage, container, false)
        if (table == null) return view
        val rvCourseList = view.findViewById<RecyclerView>(R.id.rv_list)
        val space = requireContext().dip(8)
        rvCourseList.setPadding(space, 0, space, 0)
        launch {
            val courseList = viewModel.getCourseBaseBeanListByTable(table!!.id)
            initRecyclerView(rvCourseList, courseList)
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
            val intent = Intent(activity, AddCourseActivity::class.java).apply {
                putExtra("id", -1)
                putExtra("tableId", table!!.id)
                putExtra("maxWeek", table!!.maxWeek)
                putExtra("nodes", table!!.nodes)
            }
            startActivityForResult(intent, Const.REQUEST_CODE_ADD_COURSE)
        }
        (activity as? ScheduleManageActivity)?.subButton?.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                    .setTitle("提示")
                    .setMessage("真的要清空课表吗？这将无法恢复。")
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.sure) { _, _ ->
                        launch {
                            try {
                                viewModel.clearTable(table!!.id)
                                adapter.data.clear()
                                adapter.notifyDataSetChanged()
                                Toasty.success(requireActivity(), "操作成功~").show()
                            } catch (e: Exception) {
                                Toasty.error(requireActivity(), "操作失败>_<${e.message}").show()
                            }
                        }
                    }
                    .show()
        }
    }

    private fun initRecyclerView(rvCourseList: RecyclerView, data: MutableList<CourseBaseBean>) {
        rvCourseList.layoutManager = StaggeredGridLayoutManager(if (ViewUtils.getScreenInfo(requireContext())[0] < dimenPxSize(R.dimen.wide_screen)) {
            2
        } else {
            4
        }, RecyclerView.VERTICAL)
        adapter = CourseListAdapter(R.layout.item_course_list, data)
        rvCourseList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val space = context!!.dip(8)
                outRect.set(space, space, space, space)
            }
        })
        adapter.setOnItemClickListener { _, _, position ->
            requireActivity().start<AddCourseActivity> {
                putExtra("id", data[position].id)
                putExtra("tableId", data[position].tableId)
                putExtra("maxWeek", table!!.maxWeek)
                putExtra("nodes", table!!.nodes)
            }
        }
        adapter.setOnItemLongClickListener { _, _, position ->
            MaterialAlertDialogBuilder(requireContext())
                    .setTitle("提示")
                    .setMessage("确定要删除该课程吗？它的所有时间段都将会被删除。")
                    .setPositiveButton(R.string.sure) { _, _ ->
                        launch {
                            viewModel.deleteCourse(data[position])
                            adapter.removeAt(position)
                            Toasty.success(requireContext(), "删除成功~").show()
                            val list = viewModel.getScheduleWidgetIds()
                            val appWidgetManager = AppWidgetManager.getInstance(requireActivity().applicationContext)
                            list.forEach {
                                when (it.detailType) {
                                    0 -> appWidgetManager.notifyAppWidgetViewDataChanged(it.id, R.id.lv_schedule)
                                    1 -> appWidgetManager.notifyAppWidgetViewDataChanged(it.id, R.id.lv_course)
                                }
                            }
                        }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            return@setOnItemLongClickListener true
        }
        adapter.addHeaderView(AppCompatTextView(requireContext()).apply {
            text = "轻触编辑，长按删除"
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setPadding(0, dip(8), 0, dip(8))
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        })
        adapter.addFooterView(View(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dip(240))
        })
        adapter.setEmptyView(LinearLayoutCompat(requireContext()).apply {
            id = R.id.anko_empty_view
            orientation = LinearLayoutCompat.VERTICAL
            setPadding(0, dip(72), 0, 0)
            addView(AppCompatImageView(requireContext()).apply {
                setImageResource(R.drawable.ic_schedule_empty)
            }, LinearLayoutCompat.LayoutParams.WRAP_CONTENT, dip(200))
            addView(AppCompatTextView(context).apply {
                text = "还没有添加任何课程哦"
                gravity = Gravity.CENTER
            }, LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dip(16)
            })
        })
        rvCourseList.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == Const.REQUEST_CODE_ADD_COURSE) {
            data?.extras?.getParcelable<CourseBaseBean>("course")?.let {
                adapter.addData(it)
            }
        }
    }
}
