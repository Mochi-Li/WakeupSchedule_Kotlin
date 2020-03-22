package com.suda.yzune.wakeupschedule.schedule_import

import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.schedule_import.bean.SchoolInfo
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter
import splitties.dimensions.dip

class SchoolImportListAdapter(data: MutableList<SchoolInfo>) :
        BaseQuickAdapter<SchoolInfo, BaseViewHolder>(0, data),
        StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {

    private val map = mapOf(
            Common.TYPE_ZF to "正方教务",
            Common.TYPE_ZF_1 to "正方教务 1",
            Common.TYPE_ZF_NEW to "新正方教务",
            Common.TYPE_URP to "URP 系统",
            Common.TYPE_URP_NEW to "新 URP 系统",
            Common.TYPE_URP_NEW_AJAX to "新 URP 系统 1",
            Common.TYPE_QZ to "强智教务 1",
            Common.TYPE_QZ_OLD to "旧强智教务",
            Common.TYPE_QZ_CRAZY to "强智教务 4",
            Common.TYPE_QZ_BR to "强智教务 2",
            Common.TYPE_QZ_WITH_NODE to "强智教务 3",
            Common.TYPE_QZ_2017 to "强智教务 5",
            Common.TYPE_CF to "乘方教务",
            Common.TYPE_JZ to "金智教务",
            Common.TYPE_UMOOC to "优慕课在线",
            Common.TYPE_PKU to "", // 北京大学
            Common.TYPE_BNUZ to "", // 北京师范大学珠海分校
            Common.TYPE_HNIU to "", // 湖南信息职业技术学院
            Common.TYPE_HNUST to "", // 湖南科技大学
            Common.TYPE_WHU to "", // 武汉大学
            Common.TYPE_ECJTU to "by @Preciously", // 华东交通大学
            Common.TYPE_JNU to "by @Jiuh-star", // 暨南大学
            Common.TYPE_HUNNU to "by @fearc", // 湖南师范大学
            Common.TYPE_SHU to "by @Deep Sea", // 上海大学
            Common.TYPE_LOGIN to "", // 模拟登录方式
            Common.TYPE_MAINTAIN to "不可用" // 维护状态，暂不可用
    )

    private val thanksMap = mapOf(
            "华中科技大学" to "Lyt99",
            "清华大学" to "RikaSugisawa",
            "上海大学" to "Deep Sea",
            "吉林大学" to "颩欥殘膤",
            "西北工业大学" to "ludoux",
            "苏州大学" to "Y."
    )

    override fun getHeaderId(position: Int): Long {
        return getItem(position).sortKey[0].toLong()
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_school_name_head, parent, false)
        return object : RecyclerView.ViewHolder(view) {}
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mHead = holder.itemView.findViewById<AppCompatTextView>(R.id.mHead)
        mHead.text = getItem(position).sortKey
        val myGrad = mHead.background as GradientDrawable
        myGrad.setColor(getCustomizedColor(position % 9))
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LinearLayoutCompat(parent.context).apply {
            id = R.id.anko_layout
            val outValue = TypedValue()
            context.theme.resolveAttribute(R.attr.selectableItemBackground, outValue, true)
            setBackgroundResource(outValue.resourceId)

            gravity = Gravity.CENTER_VERTICAL

            addView(AppCompatTextView(context).apply {
                id = R.id.anko_text_view
                textSize = 14f
                gravity = Gravity.CENTER_VERTICAL
                setLines(1)
            }, LinearLayoutCompat.LayoutParams(0, LinearLayoutCompat.LayoutParams.WRAP_CONTENT).apply {
                marginStart = dip(16)
                marginEnd = dip(16)
                weight = 1f
            })

            addView(AppCompatTextView(context).apply {
                id = R.id.anko_tv_value
                gravity = Gravity.CENTER_VERTICAL
                textSize = 12f
            }, LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT).apply {
                marginStart = dip(16)
                marginEnd = dip(32)
            })
        }
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, view.dip(64))
        return createBaseViewHolder(view)
    }

    override fun convert(helper: BaseViewHolder, item: SchoolInfo) {
        helper.setText(R.id.anko_text_view, item.name)
        if (item.type == Common.TYPE_LOGIN) {
            helper.setText(R.id.anko_tv_value, "by @${thanksMap[item.name]}")
        } else if (item.sortKey != "通" && item.type != Common.TYPE_HELP) {
            helper.setText(R.id.anko_tv_value, map[item.type])
        } else {
            helper.setText(R.id.anko_tv_value, "")
        }
    }

    private fun getCustomizedColor(index: Int): Int {
        val customizedColors = context.resources.getIntArray(R.array.customizedColors)
        return customizedColors[index]
    }

}