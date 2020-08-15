package com.suda.yzune.wakeupschedule.schedule_import.parser.qz

import org.jsoup.Jsoup

// 北京林业大学
class QzBJFUParser(source: String) : QzParser(source) {

    override fun getNode(nodeCount: Int): IntArray {
        if (nodeCount < 3) {
            return super.getNode(nodeCount)
        } else if (nodeCount == 3) {
            return intArrayOf(5, 5)
        } else if (nodeCount < 7) {
            return intArrayOf(nodeCount * 2 - 2, nodeCount * 2 - 1)
        } else if (nodeCount == 7) {
            return intArrayOf(12, 12)
        }
        return super.getNode(nodeCount)
    }

    override fun parseCourseName(infoStr: String): String {
        return Jsoup.parse(infoStr.substringBefore("<br>").trim()).text()
    }

}