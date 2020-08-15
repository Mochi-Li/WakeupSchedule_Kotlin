package com.suda.yzune.wakeupschedule.schedule_import.parser

import com.google.gson.JsonParser
import com.suda.yzune.wakeupschedule.schedule_import.Common
import com.suda.yzune.wakeupschedule.schedule_import.bean.Course
import com.suda.yzune.wakeupschedule.schedule_import.bean.WeekBean

// 西安工业大学
// 西北政法大学
// 大连海事大学
class SupwisdomXATUParser(source: String) : Parser(source) {

    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        var res: String = Regex(pattern = "var activity=null;[\\w\\W]*(?=table0.marshalTable)").find(source)!!.value
        res = Regex(pattern = "\\n\\s*").replace(res, "\n")
        val foundResults = Regex("^.+?;\$", RegexOption.MULTILINE).findAll(res)

        var courseName = ""
        var teacher = ""
        var room = ""
        var weekList = mutableListOf<WeekBean>()

        for (findText in foundResults) {
            val line = findText.value
            if (line.contains("var teachers =")) {
                val teacherList = arrayListOf<String>()
                JsonParser.parseString(line.substringAfter("var teachers =").substringBeforeLast(";").trim()).asJsonArray.forEach {
                    teacherList.add(it.asJsonObject["name"].asString)
                }
                teacher = teacherList.joinToString(", ")
            }
            if (line.contains("new TaskActivity(")) {
                val a = line.split(',')
                val groupName = a[a.size - 1].removePrefix("\"").removeSuffix("\");")
                courseName = if (groupName.isNotBlank()) {
                    a[a.size - 9].removeSurrounding("\"").substringBeforeLast('(') + "(组${groupName})"
                } else {
                    a[a.size - 9].removeSurrounding("\"").substringBeforeLast('(')
                }
                room = a[a.size - 7].removeSurrounding("\"")
                val weekStr = a[a.size - 6].removeSurrounding("\"")
                val weekIntList = arrayListOf<Int>()
                weekStr.forEachIndexed { index, c ->
                    if (c == '1') {
                        weekIntList.add(index)
                    }
                }
                weekList = Common.weekIntList2WeekBeanList(weekIntList)
            }
            if (line.contains("index =") && line.contains("*unitCount+")) {
                val timeInfo =
                        line.substringAfter("index =").substringBefore(";").split("*unitCount+").map { it.toInt() }
                weekList.forEach { week ->
                    val c = Course(
                            name = courseName, teacher = teacher, room = room, startNode = timeInfo[1] + 1,
                            endNode = timeInfo[1] + 1, startWeek = week.start, endWeek = week.end, type = week.type,
                            day = timeInfo[0] + 1
                    )
                    courseList.add(c)
                }
            }
        }
        courseList.sortByDescending { it.startNode }
        courseList.sortWith(compareBy({ it.day }, { it.name }, { it.startWeek }))
        for (i in 0..(courseList.size - 2)) {
            if (Common.judgeContinuousCourse(courseList[i + 1], courseList[i])) {
                courseList[i + 1].endNode = courseList[i].endNode
                courseList[i].type = -1
            }
        }
        return courseList.filter { it.type != -1 }
    }

}
