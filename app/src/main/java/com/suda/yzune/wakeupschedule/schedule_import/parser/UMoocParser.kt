package com.suda.yzune.wakeupschedule.schedule_import.parser

import com.suda.yzune.wakeupschedule.schedule_import.Common
import com.suda.yzune.wakeupschedule.schedule_import.bean.Course
import org.jsoup.Jsoup

// 优慕课在线
class UMoocParser(source: String) : Parser(source) {

    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        var id: List<String>
        var courseName: String
        var teacher = ""
        var day = 1
        var start = 1
        var end = 1
        var room = ""
        var startWeek = 1
        var endWeek = 1
        var type = 0
        var info: List<String>
        Jsoup.parse(source).getElementById("timetable").getElementsByClass("center").forEach {
            if (!it.html().contains("&lt;&lt;")) return@forEach
            info = it.html().split("&lt;&lt;")
            id = it.id().split('-')
            day = id[0].trim().toInt()
            start = id[1].trim().toInt()
            end = start
            for (str in info) {
                if (str.isBlank()) continue
                val c = str.removeSuffix("<br>").split("<br>")
                courseName = Jsoup.parse(c[0]).text().substringBefore(">>").trim()
                if (c.size < 5) {
                    room = Jsoup.parse(c[1]).text().trim()
                    teacher = Jsoup.parse(c[1]).text().trim()
                } else {
                    room = Jsoup.parse(c[1]).text().trim()
                    teacher = Jsoup.parse(c[2]).text().trim()
                }
                val weekIndex =
                        if (!Regex("\\d").containsMatchIn(c.last())) {
                            c.size - 2
                        } else {
                            c.size - 1
                        }
                if (weekIndex < 0) continue
                val weekStr = Jsoup.parse(c[weekIndex]).text().trim()
                // 北京外国语
                if (weekStr in arrayOf("全周", "双周", "单周")) {
                    when (weekStr) {
                        "全周" -> {
                            endWeek = 18
                            type = 0
                        }
                        "双周" -> {
                            endWeek = 18
                            type = 2
                        }
                        "单周" -> {
                            endWeek = 18
                            type = 1
                        }
                    }
                    courseList.add(
                            Course(
                                    name = courseName, teacher = teacher, room = room,
                                    startWeek = startWeek, endWeek = endWeek,
                                    startNode = start, endNode = end, type = type, day = day
                            )
                    )
                    continue
                }
                if (weekStr.contains(',') && !weekStr.contains('-')) {
                    val weekList = arrayListOf<Int>()
                    val weekStrList = weekStr.split(',')
                    weekStrList.forEachIndexed { index, s ->
                        if (index != weekStrList.size - 1) {
                            weekList.add(s.substringBefore('周').substringAfter('第').trim().toInt())
                        } else {
                            weekList.add(s.substringBefore('周').substringAfter('第').trim().toInt())
                        }
                    }
                    weekList.sort()
                    Common.weekIntList2WeekBeanList(weekList).forEach { weekBean ->
                        courseList.add(
                                Course(
                                        name = courseName, room = room,
                                        teacher = teacher, day = day,
                                        startNode = start, endNode = end,
                                        startWeek = weekBean.start, endWeek = weekBean.end,
                                        type = weekBean.type
                                )
                        )
                    }
                    continue
                }
                for (w in weekStr.split('.', ',')) {
                    if (w.isBlank()) continue
                    type = when {
                        w.contains('单') -> 1
                        w.contains('双') -> 2
                        else -> 0
                    }
                    val weekInfo = w.replace("单周", "")
                            .replace("双周", "")
                            .replace("全周", "")
                            .replace("双", "")
                            .replace("单", "")
                            .replace("全", "")
                            .replace(Regex("\\d*除\\d*"), "")
                            .replace("(", "")
                            .replace(")", "")
                            .replace("（", "")
                            .replace("）", "")
                            .substringBefore('周').split('-')
                    val startWeekStr = weekInfo[0].trim()
                    for (j in startWeekStr.indices.reversed()) {
                        if (!startWeekStr[j].isDigit()) {
                            startWeek = startWeekStr.substring(j + 1).toInt()
                            break
                        }
                        if (j == 0) {
                            startWeek = startWeekStr.toInt()
                        }
                    }
                    endWeek = if (weekInfo.size > 1) {
                        weekInfo.last().trim().toInt()
                    } else {
                        startWeek
                    }
                    courseList.add(
                            Course(
                                    name = courseName, teacher = teacher, room = room,
                                    startWeek = startWeek, endWeek = endWeek,
                                    startNode = start, endNode = end, type = type, day = day
                            )
                    )
                }
            }
        }
        courseList.sortByDescending { it.startNode }
        courseList.sortWith(compareBy({ it.day }, { it.name }))
        for (i in 0..(courseList.size - 2)) {
            if (Common.judgeContinuousCourse(courseList[i + 1], courseList[i])) {
                courseList[i + 1].endNode = courseList[i].endNode
                courseList[i].type = -1
            }
        }
        return courseList.filter { it.type != -1 }
    }

}