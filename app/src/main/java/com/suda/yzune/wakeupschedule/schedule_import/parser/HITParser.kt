package com.suda.yzune.wakeupschedule.schedule_import.parser

import com.suda.yzune.wakeupschedule.schedule_import.bean.Course
import org.jsoup.Jsoup

// 哈尔滨工业大学
// 北京航空航天大学
// 黑龙江建筑职业技术学院
class HITParser(source: String) : Parser(source) {

    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val doc = Jsoup.parse(source)
        val trs = doc.getElementsByClass("xfyq_con").first().getElementsByTag("tr")
        for (tr in trs) {
            val tds = tr.getElementsByTag("td")
            if (tds.size != 9) continue
            val nodeStr = tds[1].text().substringAfter('第').substringBefore('节')
            val nodeList = nodeStr.split(',', '，', '-')
            val startNode = nodeList.first().toInt()
            val endNode = nodeList.last().toInt()
            for (i in 2 until 9) {
                if (tds[i].text().isBlank()) continue
                val day = i - 1
                tds[i].children().forEach {
                    if (it.tagName() != "br") {
                        it.remove()
                    }
                }
                val list = tds[i].html().split("<br>")
                var lastIndex = -1
                for (index in list.indices) {
                    if (list[index].contains('[') && list[index].contains(']') && list[index].contains('周')) {
                        if (lastIndex != -1) {
                            getCourse(courseList, list, lastIndex, index, day, startNode, endNode)
                        }
                        lastIndex = index
                    }
                    if (index == list.size - 1) {
                        getCourse(courseList, list, lastIndex, index + 2, day, startNode, endNode)
                    }
                }
//                println(tds[i].html())
            }
        }
        return courseList
    }

    private fun getCourse(
            courseList: ArrayList<Course>,
            info: List<String>,
            lastIndex: Int,
            index: Int,
            day: Int,
            sNode: Int,
            eNode: Int
    ) {
        val courseName = info[lastIndex - 1]
        var startNode = sNode
        var endNode = eNode
        var room = info[lastIndex].substringAfterLast('周')
        if (room.contains("第") && room.contains("节")) {
            val nodeStr = room.substringAfterLast('第').substringBeforeLast('节')
            val nodeList = nodeStr.split(',', '，', '-')
            if (nodeList.isNotEmpty()) {
                startNode = nodeList.first().toInt()
                endNode = nodeList.last().toInt()
            }
        }
        if (index - lastIndex == 3) {
            room = info[lastIndex + 1]
        }
        val weekList = info[lastIndex].substringBeforeLast('周').split("周，")
        var teacher = ""
        weekList.forEach { weeks ->
            weeks.substringBefore('[').apply {
                if (this.isNotBlank()) {
                    teacher = this
                }
            }
            var weekStr = weeks.substringAfter('[')
            var type = 0
            if (weekStr.contains('单')) {
                type = 1
                weekStr = weekStr.replace("单", "")
            } else if (weekStr.contains('双')) {
                type = 2
                weekStr = weekStr.replace("双", "")
            }
            weekStr = weekStr.substringBeforeLast(']')
            weekStr.split(',', '，').forEach {
                if (it.contains('-')) {
                    val startWeek = it.substringBefore('-').trim().toInt()
                    val endWeek = it.substringAfter('-').trim().toInt()
                    courseList.add(
                            Course(
                                    name = courseName,
                                    day = day,
                                    room = room,
                                    teacher = teacher,
                                    startNode = startNode,
                                    endNode = endNode,
                                    startWeek = startWeek,
                                    endWeek = endWeek,
                                    type = type
                            )
                    )
                } else {
                    val week = it.trim().toInt()
                    courseList.add(
                            Course(
                                    name = courseName,
                                    day = day,
                                    room = room,
                                    teacher = teacher,
                                    startNode = startNode,
                                    endNode = endNode,
                                    startWeek = week,
                                    endWeek = week,
                                    type = type
                            )
                    )
                }
            }
        }
    }

}