package com.suda.yzune.wakeupschedule.schedule_import.parser

import com.suda.yzune.wakeupschedule.schedule_import.Common
import com.suda.yzune.wakeupschedule.schedule_import.bean.Course
import org.jsoup.Jsoup
import java.util.regex.Pattern

// 上海大学
class SHUParser(source: String) : Parser(source) {

    override fun generateCourseList(): List<Course> {
        val doc = Jsoup.parse(source)
        val course = arrayListOf<String>()
        val courseList = arrayListOf<Course>()
        val ele2 = doc.body().select("tr")
        for (i in 3 until ele2.size) {
            if (ele2[i].getElementsByTag("td").text().isBlank()) {
                break
            }
            course.add(ele2[i].getElementsByTag("td").text())
        }
        for (i in 0 until course.size - 1) {
            val list = getInformation(course[i])
            val courseName = list[1]
            for (j in 4 until list.size) {
                val day = Common.getNodeInt(list[j][0].toString())
                val startNode = list[j].substring(1, list[j].indexOf("-")).toInt()
                val endNode = list[j].substring(list[j].indexOf("-") + 1).toInt()
                val type = when {
                    list[j].contains('单') -> 1
                    list[j].contains('双') -> 2
                    else -> 0
                }
                courseList.add(
                        Course(
                                name = courseName, day = day, room = list[3], teacher = list[2],
                                startWeek = 1, endWeek = 10, startNode = startNode,
                                endNode = endNode, type = type
                        )
                )
            }
        }
        return courseList
    }

    private fun getInformation(info: String): List<String> {
        val strList = listOf(*info.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        val list = ArrayList<String>()
        list.add(strList[1])
        list.add(strList[2])
        list.add(strList[4])
        list.add(strList[strList.size - 4])
        val regex = "[一二三四五六七日][0-9]+-[0-9]+"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(info)
        var courseTime: MutableList<String> = ArrayList()
        while (matcher.find()) {
            courseTime.add(matcher.group())
        }
        if (courseTime.isNotEmpty()) {
            courseTime = courseTime.subList(0, courseTime.size - 1)
        }
        list.addAll(courseTime)
        return list
    }

}