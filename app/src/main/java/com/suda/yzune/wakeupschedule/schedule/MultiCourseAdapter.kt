package com.suda.yzune.wakeupschedule.schedule

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.suda.yzune.wakeupschedule.bean.CourseBean

class MultiCourseAdapter(manager: FragmentManager, val data: List<CourseBean>) : FragmentStatePagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return CourseDetailFragment.newInstance(data[position], true)
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun saveState(): Parcelable? {
        return null
    }
}