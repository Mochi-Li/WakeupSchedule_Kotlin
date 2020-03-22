package com.suda.yzune.wakeupschedule.schedule_import.parser

import com.suda.yzune.wakeupschedule.schedule_import.bean.Course

class WHUParser(source: String) : Parser(source) {

    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        var fragments: List<String>
        var courseName: String
        var teacher: String
        var room: String
        var day = 1
        var startWeek = 1
        var endWeek = 1
        var startNode = 1
        var endNode = 1
        source.substringAfter("var lessonName =")
                .split("var lessonName =")
                .forEach {
                    fragments = it.substringBefore("var academicTeach").split("\n")
                    courseName = fragments[0].substringBefore("\";//课程名").substringAfter("\"").trim()
                    day = fragments[1].substringAfter("\"").substringBefore("\"").trim().toInt()
                    startWeek = fragments[3].substringAfter("\"").substringBefore("\"").trim().toInt()
                    endWeek = fragments[4].substringAfter("\"").substringBefore("\"").trim().toInt()
                    startNode = fragments[6].substringAfter("\"").substringBefore("\"").trim().toInt()
                    endNode = fragments[7].substringAfter("\"").substringBefore("\"").trim().toInt()
                    room = fragments[9].substringAfter("\"").substringBefore("\"").trim()
                    teacher = fragments[11].substringAfter("\"").substringBefore("\"").trim()
                    courseList.add(
                            Course(
                                    name = courseName, room = room, day = day, teacher = teacher,
                                    startNode = startNode, endNode = endNode, startWeek = startWeek, endWeek = endWeek,
                                    type = 0
                            )
                    )
                }
        return courseList
    }

}