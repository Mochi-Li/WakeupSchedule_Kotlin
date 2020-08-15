package com.suda.yzune.wakeupschedule.schedule_settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.suda.yzune.wakeupschedule.BuildConfig
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.base_view.BaseListFragment
import com.suda.yzune.wakeupschedule.intro.AboutActivity
import com.suda.yzune.wakeupschedule.settings.AdvancedSettingsActivity
import com.suda.yzune.wakeupschedule.settings.SettingItemAdapter
import com.suda.yzune.wakeupschedule.settings.items.BaseSettingItem
import com.suda.yzune.wakeupschedule.settings.items.VerticalItem
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.getPrefer
import es.dmoral.toasty.Toasty

class ScheduleSettingsFragment : BaseListFragment() {

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
        items.add(VerticalItem(R.string.setting_schedule_config, "设置学期周数、当前周、上课时间、一天节数、一周开始日等，课表名称也是在这里改哦"))
        items.add(VerticalItem(R.string.setting_schedule_appearance, "背景、文字颜色和大小、小格子高度和不透明度、是否显示周末……这些都可以在这里自定义哦，未来会陆续开放更多的自定义设置"))
        items.add(VerticalItem(R.string.setting_utils, "调课可能会更方便些"))
        items.add(VerticalItem(R.string.setting_widget, "桌面小部件一些常见问题"))
        when {
            BuildConfig.CHANNEL == "google" -> {
                items.add(VerticalItem(R.string.setting_advanced_func, "如果想支持一下社团和开发者\n请去支付宝18862196504\n高级功能会持续更新~\n采用诚信授权模式ヾ(=･ω･=)o", keys = listOf("高级")))
            }
            BuildConfig.CHANNEL == "huawei" && !requireContext().getPrefer().getBoolean(Const.KEY_SHOW_DONATE, false) -> {
                items.add(VerticalItem(R.string.setting_advanced_func, "高级功能会持续更新~", keys = listOf("高级")))
            }
            else -> {
                items.add(VerticalItem(R.string.setting_advanced_func, "解锁赞助一下社团和开发者ヾ(=･ω･=)o\n高级功能会持续更新~\n采用诚信授权模式", keys = listOf("高级")))
            }
        }
        items.add(VerticalItem(R.string.title_about, "了解背后的一些故事~"))
        items.add(VerticalItem(R.string.setting_blank, "\n\n\n"))
    }

    private fun onVerticalItemClick(item: VerticalItem) {
        when (item.title) {
            R.string.setting_schedule_config -> Navigation.findNavController(requireView()).navigate(R.id.action_scheduleSettingsFragment_to_tableConfigFragment)
            R.string.setting_schedule_appearance -> Navigation.findNavController(requireView()).navigate(R.id.action_scheduleSettingsFragment_to_mainStyleFragment)
            R.string.title_about -> startActivity(Intent(requireActivity(), AboutActivity::class.java))
            R.string.setting_utils -> Toasty.info(requireContext(), "敬请期待").show()
            R.string.setting_advanced_func -> startActivity(Intent(requireActivity(), AdvancedSettingsActivity::class.java))
            R.string.setting_widget -> Navigation.findNavController(requireView()).navigate(R.id.action_scheduleSettingsFragment_to_introAppWidgetFragment)
        }
    }
}