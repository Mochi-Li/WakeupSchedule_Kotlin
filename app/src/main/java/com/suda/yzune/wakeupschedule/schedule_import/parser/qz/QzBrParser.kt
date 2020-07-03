package com.suda.yzune.wakeupschedule.schedule_import.parser.qz

import org.jsoup.Jsoup

class QzBrParser(source: String) : QzParser(source) {

    override fun parseCourseName(infoStr: String): String {
        return Jsoup.parse(infoStr.substringBefore("<br>").trim()).text()
    }

}