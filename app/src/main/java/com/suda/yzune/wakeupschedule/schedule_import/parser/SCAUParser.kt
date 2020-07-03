package com.suda.yzune.wakeupschedule.schedule_import.parser

import com.suda.yzune.wakeupschedule.schedule_import.bean.Course
import org.jsoup.Jsoup

class SCAUParser(source: String) : Parser(source) {

    override fun generateCourseList(): List<Course> {

        val courseList = arrayListOf<Course>()
        val doc = Jsoup.parse(source)
        val trs = doc.getElementsByTag("table")
                .select("[border=1][bordercolor=#000000]")
                .first().getElementsByTag("tr").drop(2)
        trs.forEachIndexed { index, tr ->
            val tds = tr.getElementsByTag("td").select("[valign=top]")
            tds.forEachIndexed td@{ dayIndex, td ->
                val lst = td.html().split("<br>")
                if (lst.size <= 1) return@td
                val name = lst[0].substringBefore("：")
                val teacher = lst[0].substringAfter("：")
                val weekList = lst[2].substringBefore("周").split(",")
                val type = when {
                    lst[2].contains("单") -> 1
                    lst[2].contains("双") -> 2
                    else -> 0
                }
                var startWeek = 1
                var endWeek = 20
                weekList.forEach {
                    if (it.contains('-')) {
                        val weeks = it.split('-')
                        if (weeks.isNotEmpty()) {
                            startWeek = weeks[0].toInt()
                        }
                        if (weeks.size > 1) {
                            endWeek = weeks[1].toInt()
                        }
                    } else {
                        startWeek = it.toInt()
                        endWeek = it.toInt()
                    }
                    courseList.add(
                            Course(
                                    name = name, teacher = teacher,
                                    room = lst[1], day = dayIndex + 1,
                                    startNode = index * 2 + 1,
                                    endNode = index * 2 + 2,
                                    startWeek = startWeek, endWeek = endWeek,
                                    type = type
                            )
                    )
                }
            }
        }
        return courseList
    }

}