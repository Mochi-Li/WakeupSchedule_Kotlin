package com.suda.yzune.wakeupschedule.course_add

import android.text.Editable
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.bean.CourseEditBean
import com.suda.yzune.wakeupschedule.schedule_import.Common
import com.suda.yzune.wakeupschedule.utils.CourseUtils

class AddCourseAdapter(layoutResId: Int, data: MutableList<CourseEditBean>) :
        BaseQuickAdapter<CourseEditBean, BaseViewHolder>(layoutResId, data) {

    private var mListener: OnItemEditTextChangedListener? = null

    fun setListener(listener: OnItemEditTextChangedListener) {
        mListener = listener
    }

    override fun convert(helper: BaseViewHolder, item: CourseEditBean) {
        //helper.setText(R.id.tv_item, "${helper.layoutPosition}")
        helper.setText(R.id.et_room, item.room)
        helper.setText(R.id.et_teacher, item.teacher)
        helper.setText(R.id.et_weeks, Common.weekIntList2WeekBeanListString(context, item.weekList.value!!))
        helper.setText(R.id.et_time, context.getString(R.string.add_course_time, CourseUtils.getDayStr(context, item.time.value!!.day), item.time.value!!.startNode, item.time.value!!.endNode))
    }

    interface OnItemEditTextChangedListener {
        fun onEditTextAfterTextChanged(editable: Editable, position: Int, what: String)
    }

    override fun onDetachedFromRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mListener = null
    }

}