package com.suda.yzune.wakeupschedule.schedule_import

import android.content.Context
import com.suda.yzune.wakeupschedule.R
import com.suda.yzune.wakeupschedule.bean.CourseBaseBean
import com.suda.yzune.wakeupschedule.schedule_import.bean.Course
import com.suda.yzune.wakeupschedule.schedule_import.bean.WeekBean

object Common {

    const val TYPE_HELP = "help"
    const val TYPE_ZF = "zf"
    const val TYPE_ZF_1 = "zf_1"
    const val TYPE_ZF_NEW = "zf_new"
    const val TYPE_URP = "urp"
    const val TYPE_UMOOC = "umooc"
    const val TYPE_URP_NEW = "urp_new"
    const val TYPE_URP_NEW_AJAX = "urp_new_ajax"
    const val TYPE_QZ = "qz"
    const val TYPE_QZ_OLD = "qz_old"
    const val TYPE_QZ_CRAZY = "qz_crazy"
    const val TYPE_QZ_BR = "qz_br"
    const val TYPE_QZ_BJFU = "qz_bjfu"
    const val TYPE_QZ_WITH_NODE = "qz_with_node"
    const val TYPE_QZ_2017 = "qz_2017" // 华南农业大学
    const val TYPE_CF = "cf"
    const val TYPE_VATUU = "vatuu"
    const val TYPE_PKU = "pku" // 北京大学
    const val TYPE_BNUZ = "bnuz" // 北京师范大学珠海分校
    const val TYPE_HNIU = "hniu" // 湖南信息职业技术学院
    const val TYPE_HNUST = "hnust" // 湖南科技大学
    const val TYPE_JNU = "jnu" // 暨南大学
    const val TYPE_HUNNU = "hunnu" // 湖南师范大学
    const val TYPE_ECJTU = "ecjtu" // 华东交通大学
    const val TYPE_SHU = "shu"// 上海大学
    const val TYPE_SIT = "sit"// 上海应用技术大学
    const val TYPE_CQMU = "cqmu_shuwei" // 重庆医科大学
    const val TYPE_LNTU = "lntu_shuwei" // 辽宁工程技术大学
    const val TYPE_XATU = "xatu_shuwei" // 西安工业大学
    const val TYPE_AHNU = "ahnu" // 安徽师范大学
    const val TYPE_SCAU = "scau" // 四川农业大学
    const val TYPE_SDU = "sdu" // 山东大学
    const val TYPE_JZ = "jz" // 金智教务
    const val TYPE_JZ_1 = "jz_1"
    const val TYPE_HAUST = "haust" // 河南科技大学
    const val TYPE_HIT = "hit" // 哈尔滨工业大学
    const val TYPE_LOGIN = "login" // 模拟登录方式
    const val TYPE_MAINTAIN = "maintain" // 维护状态，暂不可用

    val nodePattern = Regex("""\(\d{1,2}[-]*\d*节""")
    val nodePattern1 = Regex("""\d{1,2}[~]*\d*节""")
    val nodePattern2 = Regex("""(^\d.*)节""")

    val weekPattern = Regex("""\{第\d{1,2}[-]*\d*周""")
    val weekPattern1 = Regex("""\d{1,2}[-]*\d*""")
    val weekPattern2 = Regex("""\d{1,2}周""")

    val chineseWeekList = arrayOf("", "周一", "周二", "周三", "周四", "周五", "周六", "周日")
    val otherHeader = arrayOf("时间", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日", "早晨", "上午", "下午", "晚上")
    val courseProperty = arrayOf(
            "任选",
            "限选",
            "实践选修",
            "必修课",
            "选修课",
            "必修",
            "选修",
            "专基",
            "专选",
            "公必",
            "公选",
            "义修",
            "选",
            "必",
            "主干",
            "专限",
            "公基",
            "值班",
            "通选",
            "思政必",
            "思政选",
            "自基必",
            "自基选",
            "语技必",
            "语技选",
            "体育必",
            "体育选",
            "专业基础课",
            "双创必",
            "双创选",
            "新生必",
            "新生选",
            "学科必修",
            "学科选修",
            "通识必修",
            "通识选修",
            "公共基础",
            "第二课堂",
            "学科实践",
            "专业实践",
            "专业必修",
            "辅修",
            "专业选修",
            "外语",
            "方向",
            "专业必修课",
            "全选",
            "专业", "学必", "学选", "通必"
    )

    private val headerNodePattern = Regex("""第.*节""")

    fun weekIntList2WeekBeanListString(context: Context, input: MutableList<Int>): String {
        val list = weekIntList2WeekBeanList(input)
        return list.joinToString {
            val typeString = when (it.type) {
                1 -> " ${context.getString(R.string.odd)}"
                2 -> " ${context.getString(R.string.even)}"
                else -> ""
            }
            context.getString(R.string.week_bean_to_string, it.start, it.end, typeString)
        }
    }

    fun weekIntList2WeekBeanList(input: MutableList<Int>): MutableList<WeekBean> {
        var reset = 0
        var temp = WeekBean(0, 0, -1)
        val list = arrayListOf<WeekBean>()
        for (i in input.indices) {
            if (reset == 1) {
                list.add(temp)
                temp = WeekBean(0, 0, -1)
                reset = 0
            }
            if (i < input.size - 1) {
                if (temp.type == 0 && input[i + 1] - input[i] == 1) temp.end = input[i + 1]
                else if ((temp.type == 1 || temp.type == 2) && input[i + 1] - input[i] == 2)
                    temp.end = input[i + 1]
                else if (temp.type != -1) {
                    reset = 1
                }
            }
            if (i < input.size - 1 && temp.type == -1) {
                temp.start = input[i]
                when (input[i + 1] - input[i]) {
                    1 -> {
                        temp.type = 0
                        temp.end = input[i + 1]
                    }
                    2 -> {
                        temp.type = if (input[i] % 2 != 0) 1 else 2
                        temp.end = input[i + 1]
                    }
                    else -> {
                        temp.end = input[i]
                        temp.type = 0
                        reset = 1
                    }
                }
            }
            if (i == input.size - 1 && temp.type != -1) list.add(temp)
            if (i == input.size - 1 && temp.type == -1) {
                temp.start = input[i]
                temp.end = input[i]
                temp.type = 0
                list.add(temp)
            }
        }
        return list
    }

    fun findExistedCourseId(list: List<CourseBaseBean>, name: String): Int {
        val result = list.findLast {
            it.courseName == name
        }
        return result?.id ?: -1
    }

    fun parseHeaderNodeString(str: String): Int {
        var node = -1
        if (headerNodePattern.matches(str)) {
            val nodeStr = str.substring(1, str.length - 1)
            node = try {
                nodeStr.toInt()
            } catch (e: Exception) {
                getNodeInt(nodeStr)
            }
        }
        return node
    }

    fun getWeekFromChinese(chineseWeek: String): Int {
        for (i in chineseWeekList.indices) {
            if (chineseWeekList[i] == chineseWeek) {
                return i
            }
        }
        return 0
    }

    fun countStr(str1: String, str2: String): Int {
        var times = 0
        var startIndex = 0
        var findIndex = str1.indexOf(str2, startIndex)
        while (findIndex != -1 && findIndex != str1.length - 1) {
            times += 1
            startIndex = findIndex + 1
            findIndex = str1.indexOf(str2, startIndex)
        }
        if (findIndex == str1.length - 1) {
            times += 1
        }
        return times
    }

    fun getNodeStr(node: Int): String {
        return when (node) {
            1 -> "一"
            2 -> "二"
            3 -> "三"
            4 -> "四"
            5 -> "五"
            6 -> "六"
            7 -> "七"
            8 -> "八"
            9 -> "九"
            10 -> "十"
            11 -> "十一"
            12 -> "十二"
            13 -> "十三"
            14 -> "十四"
            15 -> "十五"
            16 -> "十六"
            else -> ""
        }
    }

    fun getNodeInt(nodeStr: String): Int {
        return when (nodeStr) {
            "一" -> 1
            "二" -> 2
            "三" -> 3
            "四" -> 4
            "五" -> 5
            "六" -> 6
            "七" -> 7
            "日" -> 7
            "八" -> 8
            "九" -> 9
            "十" -> 10
            "十一" -> 11
            "十二" -> 12
            "十三" -> 13
            "十四" -> 14
            "十五" -> 15
            "十六" -> 16
            "十七" -> 17
            "十八" -> 18
            "十九" -> 19
            "二十" -> 20
            else -> -1
        }
    }

    fun judgeContinuousCourse(pre: Course, current: Course): Boolean {
        return pre.name == current.name
                && pre.day == current.day
                && pre.room == current.room
                && pre.teacher == current.teacher
                && pre.startWeek == current.startWeek
                && pre.endWeek == current.endWeek
                && pre.type == current.type
                && pre.endNode == current.startNode - 1
    }

    // 处理多个时间间隔，可自定义列表间隔与时间间隔字符串
    // 例如：输入 '1-2' ，输出 [Pair(1, 2)]
    //      输入 '2' ，输出 [Pair(2, 2)]
    //      输入 '' ，输出 []
    //      输入 '1-2,2-3,4' ，输出 [Pair(1, 2), Pair(2, 3), Pair(4, 4)]
    fun parseTimePeriodList(str: String, listSplitSymbol: String = ",", timeSplitSymbol: String = "-") = if (str.isNotEmpty() && str.isNotBlank()) {
        val text = str.trim()
        if (listSplitSymbol in text) {
            val splitArray = text.split(listSplitSymbol)
            Array(splitArray.size) { i ->
                parseTimePeriod(splitArray[i], timeSplitSymbol)
            }
        } else {
            arrayOf(parseTimePeriod(text, timeSplitSymbol))
        }
    } else {
        emptyArray()
    }

    // 处理单个时间间隔，可自定义时间间隔字符串
    // 例如：输入 '1-2' ，输出 Pair(1, 2)
    //      输入 '2' ，输出 Pair(2, 2)
    fun parseTimePeriod(str: String, splitSymbol: String = "-") = if (str.isNotEmpty() && str.isNotBlank()) {
        val text = str.trim()
        if (splitSymbol in text) {
            val splitArr = text.split(splitSymbol)
            splitArr[0].toInt() to splitArr[1].toInt()
        } else {
            text.toInt() to text.toInt()
        }
    } else {
        throw IllegalArgumentException("Empty Parse Text to Time Period!")
    }
}