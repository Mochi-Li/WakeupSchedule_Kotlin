package com.suda.yzune.wakeupschedule.schedule_import.parser

import com.suda.yzune.wakeupschedule.schedule_import.bean.Course
import org.jsoup.Jsoup

class SDUParser(source: String) : Parser(source) {

    override fun generateCourseList(): List<Course> {

        val courseList = arrayListOf<Course>()
        val doc = Jsoup.parse(source)
        val trs = doc.getElementById("ysjddDataTableId")
                .getElementsByTag("tr").drop(1)
        trs.forEach { tr ->
            val tds = tr.getElementsByTag("td")
            val name = tds[2].text()
            val room = tds.last().text()
            val day = tds[9].text().toInt()
            val startNode = tds[10].text().toInt()
            val teacher = tds[7].text()
            val weekList = tds[8].text().substringBefore("周").split(",")
            var startWeek = 1
            var endWeek = 20
            weekList.forEach {
                val type = when {
                    it.contains("单") -> 1
                    it.contains("双") -> 2
                    else -> 0
                }
                if (it.contains('-')) {
                    val weeks = it.split('-')
                    if (weeks.isNotEmpty()) {
                        startWeek = weeks[0].toInt()
                    }
                    if (weeks.size > 1) {
                        endWeek = weeks[1].toInt()
                    }
                } else {
                    try {
                        startWeek = it.toInt()
                        endWeek = it.toInt()
                    } catch (e: Exception) {
                        startWeek = 1
                        endWeek = 20
                    }
                }
                courseList.add(
                        Course(
                                name = name, teacher = teacher,
                                room = room, day = day,
                                startNode = startNode,
                                endNode = startNode,
                                startWeek = startWeek, endWeek = endWeek,
                                type = type
                        )
                )
            }
        }
        return courseList
    }

}
