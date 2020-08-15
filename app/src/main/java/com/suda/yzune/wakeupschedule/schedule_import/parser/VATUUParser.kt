package com.suda.yzune.wakeupschedule.schedule_import.parser

import com.suda.yzune.wakeupschedule.schedule_import.bean.Course
import org.jsoup.Jsoup

class VATUUParser(source: String) : Parser(source) {

    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val doc = Jsoup.parse(source)
        val table = doc.getElementsByClass("table_border").first()
        if (table.id() == "table3") {
            throw Exception("请选择不显示学分的课表视图")
        }
        val trs = table.getElementsByTag("tr")
        trs.forEachIndexed { node, tr ->
            if (node == 0) return@forEachIndexed
            val tds = tr.getElementsByTag("td").drop(1)
            tds.forEachIndexed forTd@{ day, td ->
                if (day == 0 || td.text().isBlank()) return@forTd
                val lst = td.html().split("&nbsp;", "<br>")
                val name = lst[1].substringBeforeLast("（")
                val teacher = lst[1].substringAfterLast("（").substringBeforeLast("）")
                val room = lst[3]
                val weekList = lst[2].split(',')
                var startWeek = 0
                var endWeek = 0
                var type = 0
                weekList.forEach {
                    if (it.contains('-')) {
                        val weeks = it.split('-')
                        if (weeks.isNotEmpty()) {
                            startWeek = weeks[0].toInt()
                        }
                        if (weeks.size > 1) {
                            type = when {
                                weeks[1].contains('单') -> 1
                                weeks[1].contains('双') -> 2
                                else -> 0
                            }
                            endWeek = weeks[1].substringBefore('周').toInt()
                        }
                    } else {
                        startWeek = it.substringBefore('周').toInt()
                        endWeek = it.substringBefore('周').toInt()
                        type = 0
                    }
                    courseList.add(
                            Course(
                                    name, day, room, teacher, node, node, startWeek, endWeek, type
                            )
                    )
                }
            }
        }
        return courseList
    }

}
