package com.suda.yzune.wakeupschedule.schedule_settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.suda.yzune.wakeupschedule.base_view.BaseListFragment
import com.suda.yzune.wakeupschedule.settings.SettingItemAdapter
import com.suda.yzune.wakeupschedule.settings.items.BaseSettingItem
import com.suda.yzune.wakeupschedule.settings.items.VerticalItem

class ToolsFragment : BaseListFragment() {

    private val mAdapter = SettingItemAdapter()
    private val viewModel by activityViewModels<ScheduleSettingsViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val items = mutableListOf<BaseSettingItem>()
        onItemsCreated(items)
        mAdapter.data = items
        mRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        mRecyclerView.itemAnimator?.changeDuration = 250
        mRecyclerView.adapter = mAdapter

        mAdapter.setOnItemClickListener { _, _, position ->
            when (val item = items[position]) {
                is VerticalItem -> onVerticalItemClick(item)
            }
        }
    }

    private fun onItemsCreated(items: MutableList<BaseSettingItem>) {
        items.add(VerticalItem("上下移动课程", "将第 n 节之后的课程批量上移/下移，可以用来修正导入的错误，或是添加早读午休"))
        items.add(VerticalItem("", "\n\n\n"))
    }

    private fun onVerticalItemClick(item: VerticalItem) {
        when (item.title) {
            "上下移动课程" -> ShiftCourseFragment.newInstance().show(parentFragmentManager, null)
        }
    }
}