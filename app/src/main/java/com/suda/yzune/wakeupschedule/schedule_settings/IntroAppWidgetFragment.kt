package com.suda.yzune.wakeupschedule.schedule_settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.suda.yzune.wakeupschedule.base_view.BaseListFragment
import com.suda.yzune.wakeupschedule.settings.SettingItemAdapter
import com.suda.yzune.wakeupschedule.settings.items.BaseSettingItem
import com.suda.yzune.wakeupschedule.settings.items.VerticalItem
import com.suda.yzune.wakeupschedule.utils.Utils

class IntroAppWidgetFragment : BaseListFragment() {
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

        mAdapter.setOnItemClickListener { _, itemView, position ->
            when (val item = items[position]) {
                is VerticalItem -> onVerticalItemClick(item)
            }
        }
    }

    private fun onItemsCreated(items: MutableList<BaseSettingItem>) {
        items.add(VerticalItem("如何添加小部件？", "长按桌面空白处，或者在桌面做双指捏合手势，选择桌面小工具，肯定是有的，仔细找找，实在找不到就重启手机再找。\n" +
                "P.S. 添加桌面小部件，想要确保它正常工作，最好在系统设置中，手动管理本App的后台，允许本App后台自启和后台运行。"))
        items.add(VerticalItem("如何调整小部件大小？", "如果想调整小部件整体的高度，要在桌面长按小部件来调整。华为和荣耀手机如果长按后调整不了，是第三方主题导致的，请切换回系统默认主题再调整。"))
        items.add(VerticalItem("如何调整小部件样式？", "小部件右上角有个「调整」的按钮，点它就可以了。"))
        items.add(VerticalItem("小部件刷新不及时/显示正在加载", "可能是被手机清理了后台。请在系统设置中，手动管理本 App 的后台，允许本 App 后台自启和后台运行。另外，小部件右上角有个小箭头，点击两次可以强制刷新，不需要重新放置小部件的。" +
                "\n" +
                "华为/荣耀手机设置方式：打开系统自带的手机管家 -> 应用启动管理 -> 找到 WakeUp课程表 -> 设置为手动管理后台，同时允许后台自启和后台运行。"))
        items.add(VerticalItem("更多问题", "根据反馈不定时更新"))
        items.add(VerticalItem("", "\n\n\n"))
    }

    private fun onVerticalItemClick(item: VerticalItem) {
        when (item.title) {
            "更多问题" -> Utils.openUrl(requireActivity(), "https://support.qq.com/embed/97617/faqs-more")
        }
    }
}