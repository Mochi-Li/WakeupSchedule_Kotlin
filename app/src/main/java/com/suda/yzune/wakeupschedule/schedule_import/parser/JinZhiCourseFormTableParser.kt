package com.suda.yzune.wakeupschedule.schedule_import.parser

import com.suda.yzune.wakeupschedule.schedule_import.bean.Course
import org.jsoup.Jsoup

class JinZhiCourseFormTableParser(source: String) : Parser(source) {

    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()

        var flag = false
        val span = IntArray(7)
        val doc = Jsoup.parse(source)
        val table = doc.getElementById("1") ?: doc.getElementsByClass("CourseFormTable").first()
        var nodeIndex = 0
        var weekPlace = 1
        var nodePlace = 2
        var teacherPlace = 3
        table.getElementsByTag("tr").forEach tr@{ tr ->
            if (flag) return@tr
            var index = 0
            tr.getElementsByTag("td").forEach td@{ td ->
                if (td.attr("style").contains("center")
                        || td.className() == "firstHiddenTd"
                ) return@td
                if (td.className() == "NotFixCourseTableTd" || td.className() == "commentAlign") {
                    flag = true
                    return@tr
                }
                while (span[index] >= nodeIndex) {
                    index++
                }
                span[index] += td.attr("rowspan").toInt()
                if (td.text().isBlank()) return@td
                val lst = td.html().substringAfterLast("</div>&nbsp;")
                        .replace("&nbsp;<br>", "<br>")
                        .replace("<br>&nbsp;", "<br>")
                        .substringBeforeLast("</td>").split("<hr>")
                        .map { it.split("<br>", "&nbsp;") }
                lst.forEach { c ->
                    val day = index + 1
                    val name = c[0].substringBeforeLast("[").trim()
                    if (c[1].isEmpty() || !c[1][0].isDigit()) {
                        weekPlace = 2
                        nodePlace = 3
                        teacherPlace = 1
                    }
                    val nodes = c[nodePlace].substringAfter("第").substringBefore("节")
                            .split("-").map { it.toInt() }
                    c[weekPlace].split(",").forEach { weekStr ->
                        val type = when {
                            weekStr.contains("单") -> 1
                            weekStr.contains("双") -> 2
                            else -> 0
                        }
                        val weeks = weekStr.substringBefore("周").split("-").map { it.toInt() }
                        courseList.add(
                                Course(
                                        name = name,
                                        teacher = c[teacherPlace].trim(),
                                        room = c[4].trim(),
                                        startNode = nodes[0],
                                        endNode = nodes.last(),
                                        startWeek = weeks[0],
                                        endWeek = weeks.last(),
                                        type = type,
                                        day = day
                                )
                        )
                    }
                }
                index++
            }
            nodeIndex++
        }

        return courseList
    }

}