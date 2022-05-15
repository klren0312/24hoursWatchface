package com.zzes.stock

import java.util.*
import kotlin.math.floor

object LunarDateUtil {
    private const val FALSE = 0
    private val lunarInfo = arrayListOf(
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260,
        0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255,
        0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40,
        0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0,
        0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4,
        0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0,
        0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0,
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570,
        0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4,
        0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6,
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a,
        0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50,
        0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552,
        0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9,
        0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60,
        0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
        0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0,
        0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577,
        0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0,
        0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0,
        0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0,
        0x0a2e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0,
        0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4,
        0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6,
        0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0,
        0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50,
        0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160,
        0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0,
        0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252, 0x0d520)

    private val solarMonth = arrayListOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    private val gan = arrayListOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
    private val zhi = arrayListOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
    private val animals = arrayListOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")
    private val solarTerm = arrayListOf("小寒", "大寒", "立春", "雨水", "惊蛰", "春分", "清明", "谷雨", "立夏", "小满", "芒种", "夏至", "小暑", "大暑", "立秋", "处暑", "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪", "冬至")
    private val sTermInfo = arrayListOf(
        "9778397bd097c36b0b6fc9274c91aa", "97b6b97bd19801ec9210c965cc920e",
        "97bcf97c3598082c95f8c965cc920f", "97bd0b06bdb0722c965ce1cfcc920f",
        "b027097bd097c36b0b6fc9274c91aa", "97b6b97bd19801ec9210c965cc920e",
        "97bcf97c359801ec95f8c965cc920f", "97bd0b06bdb0722c965ce1cfcc920f",
        "b027097bd097c36b0b6fc9274c91aa", "97b6b97bd19801ec9210c965cc920e",
        "97bcf97c359801ec95f8c965cc920f", "97bd0b06bdb0722c965ce1cfcc920f",
        "b027097bd097c36b0b6fc9274c91aa", "9778397bd19801ec9210c965cc920e",
        "97b6b97bd19801ec95f8c965cc920f", "97bd09801d98082c95f8e1cfcc920f",
        "97bd097bd097c36b0b6fc9210c8dc2", "9778397bd197c36c9210c9274c91aa",
        "97b6b97bd19801ec95f8c965cc920e", "97bd09801d98082c95f8e1cfcc920f",
        "97bd097bd097c36b0b6fc9210c8dc2", "9778397bd097c36c9210c9274c91aa",
        "97b6b97bd19801ec95f8c965cc920e", "97bcf97c3598082c95f8e1cfcc920f",
        "97bd097bd097c36b0b6fc9210c8dc2", "9778397bd097c36c9210c9274c91aa",
        "97b6b97bd19801ec9210c965cc920e", "97bcf97c3598082c95f8c965cc920f",
        "97bd097bd097c35b0b6fc920fb0722", "9778397bd097c36b0b6fc9274c91aa",
        "97b6b97bd19801ec9210c965cc920e", "97bcf97c3598082c95f8c965cc920f",
        "97bd097bd097c35b0b6fc920fb0722", "9778397bd097c36b0b6fc9274c91aa",
        "97b6b97bd19801ec9210c965cc920e", "97bcf97c359801ec95f8c965cc920f",
        "97bd097bd097c35b0b6fc920fb0722", "9778397bd097c36b0b6fc9274c91aa",
        "97b6b97bd19801ec9210c965cc920e", "97bcf97c359801ec95f8c965cc920f",
        "97bd097bd097c35b0b6fc920fb0722", "9778397bd097c36b0b6fc9274c91aa",
        "97b6b97bd19801ec9210c965cc920e", "97bcf97c359801ec95f8c965cc920f",
        "97bd097bd07f595b0b6fc920fb0722", "9778397bd097c36b0b6fc9210c8dc2",
        "9778397bd19801ec9210c9274c920e", "97b6b97bd19801ec95f8c965cc920f",
        "97bd07f5307f595b0b0bc920fb0722", "7f0e397bd097c36b0b6fc9210c8dc2",
        "9778397bd097c36c9210c9274c920e", "97b6b97bd19801ec95f8c965cc920f",
        "97bd07f5307f595b0b0bc920fb0722", "7f0e397bd097c36b0b6fc9210c8dc2",
        "9778397bd097c36c9210c9274c91aa", "97b6b97bd19801ec9210c965cc920e",
        "97bd07f1487f595b0b0bc920fb0722", "7f0e397bd097c36b0b6fc9210c8dc2",
        "9778397bd097c36b0b6fc9274c91aa", "97b6b97bd19801ec9210c965cc920e",
        "97bcf7f1487f595b0b0bb0b6fb0722", "7f0e397bd097c35b0b6fc920fb0722",
        "9778397bd097c36b0b6fc9274c91aa", "97b6b97bd19801ec9210c965cc920e",
        "97bcf7f1487f595b0b0bb0b6fb0722", "7f0e397bd097c35b0b6fc920fb0722",
        "9778397bd097c36b0b6fc9274c91aa", "97b6b97bd19801ec9210c965cc920e",
        "97bcf7f1487f531b0b0bb0b6fb0722", "7f0e397bd097c35b0b6fc920fb0722",
        "9778397bd097c36b0b6fc9274c91aa", "97b6b97bd19801ec9210c965cc920e",
        "97bcf7f1487f531b0b0bb0b6fb0722", "7f0e397bd07f595b0b6fc920fb0722",
        "9778397bd097c36b0b6fc9274c91aa", "97b6b97bd19801ec9210c9274c920e",
        "97bcf7f0e47f531b0b0bb0b6fb0722", "7f0e397bd07f595b0b0bc920fb0722",
        "9778397bd097c36b0b6fc9210c91aa", "97b6b97bd197c36c9210c9274c920e",
        "97bcf7f0e47f531b0b0bb0b6fb0722", "7f0e397bd07f595b0b0bc920fb0722",
        "9778397bd097c36b0b6fc9210c8dc2", "9778397bd097c36c9210c9274c920e",
        "97b6b7f0e47f531b0723b0b6fb0722", "7f0e37f5307f595b0b0bc920fb0722",
        "7f0e397bd097c36b0b6fc9210c8dc2", "9778397bd097c36b0b70c9274c91aa",
        "97b6b7f0e47f531b0723b0b6fb0721", "7f0e37f1487f595b0b0bb0b6fb0722",
        "7f0e397bd097c35b0b6fc9210c8dc2", "9778397bd097c36b0b6fc9274c91aa",
        "97b6b7f0e47f531b0723b0b6fb0721", "7f0e27f1487f595b0b0bb0b6fb0722",
        "7f0e397bd097c35b0b6fc920fb0722", "9778397bd097c36b0b6fc9274c91aa",
        "97b6b7f0e47f531b0723b0b6fb0721", "7f0e27f1487f531b0b0bb0b6fb0722",
        "7f0e397bd097c35b0b6fc920fb0722", "9778397bd097c36b0b6fc9274c91aa",
        "97b6b7f0e47f531b0723b0b6fb0721", "7f0e27f1487f531b0b0bb0b6fb0722",
        "7f0e397bd097c35b0b6fc920fb0722", "9778397bd097c36b0b6fc9274c91aa",
        "97b6b7f0e47f531b0723b0b6fb0721", "7f0e27f1487f531b0b0bb0b6fb0722",
        "7f0e397bd07f595b0b0bc920fb0722", "9778397bd097c36b0b6fc9274c91aa",
        "97b6b7f0e47f531b0723b0787b0721", "7f0e27f0e47f531b0b0bb0b6fb0722",
        "7f0e397bd07f595b0b0bc920fb0722", "9778397bd097c36b0b6fc9210c91aa",
        "97b6b7f0e47f149b0723b0787b0721", "7f0e27f0e47f531b0723b0b6fb0722",
        "7f0e397bd07f595b0b0bc920fb0722", "9778397bd097c36b0b6fc9210c8dc2",
        "977837f0e37f149b0723b0787b0721", "7f07e7f0e47f531b0723b0b6fb0722",
        "7f0e37f5307f595b0b0bc920fb0722", "7f0e397bd097c35b0b6fc9210c8dc2",
        "977837f0e37f14998082b0787b0721", "7f07e7f0e47f531b0723b0b6fb0721",
        "7f0e37f1487f595b0b0bb0b6fb0722", "7f0e397bd097c35b0b6fc9210c8dc2",
        "977837f0e37f14998082b0787b06bd", "7f07e7f0e47f531b0723b0b6fb0721",
        "7f0e27f1487f531b0b0bb0b6fb0722", "7f0e397bd097c35b0b6fc920fb0722",
        "977837f0e37f14998082b0787b06bd", "7f07e7f0e47f531b0723b0b6fb0721",
        "7f0e27f1487f531b0b0bb0b6fb0722", "7f0e397bd097c35b0b6fc920fb0722",
        "977837f0e37f14998082b0787b06bd", "7f07e7f0e47f531b0723b0b6fb0721",
        "7f0e27f1487f531b0b0bb0b6fb0722", "7f0e397bd07f595b0b0bc920fb0722",
        "977837f0e37f14998082b0787b06bd", "7f07e7f0e47f531b0723b0b6fb0721",
        "7f0e27f1487f531b0b0bb0b6fb0722", "7f0e397bd07f595b0b0bc920fb0722",
        "977837f0e37f14998082b0787b06bd", "7f07e7f0e47f149b0723b0787b0721",
        "7f0e27f0e47f531b0b0bb0b6fb0722", "7f0e397bd07f595b0b0bc920fb0722",
        "977837f0e37f14998082b0723b06bd", "7f07e7f0e37f149b0723b0787b0721",
        "7f0e27f0e47f531b0723b0b6fb0722", "7f0e397bd07f595b0b0bc920fb0722",
        "977837f0e37f14898082b0723b02d5", "7ec967f0e37f14998082b0787b0721",
        "7f07e7f0e47f531b0723b0b6fb0722", "7f0e37f1487f595b0b0bb0b6fb0722",
        "7f0e37f0e37f14898082b0723b02d5", "7ec967f0e37f14998082b0787b0721",
        "7f07e7f0e47f531b0723b0b6fb0722", "7f0e37f1487f531b0b0bb0b6fb0722",
        "7f0e37f0e37f14898082b0723b02d5", "7ec967f0e37f14998082b0787b06bd",
        "7f07e7f0e47f531b0723b0b6fb0721", "7f0e37f1487f531b0b0bb0b6fb0722",
        "7f0e37f0e37f14898082b072297c35", "7ec967f0e37f14998082b0787b06bd",
        "7f07e7f0e47f531b0723b0b6fb0721", "7f0e27f1487f531b0b0bb0b6fb0722",
        "7f0e37f0e37f14898082b072297c35", "7ec967f0e37f14998082b0787b06bd",
        "7f07e7f0e47f531b0723b0b6fb0721", "7f0e27f1487f531b0b0bb0b6fb0722",
        "7f0e37f0e366aa89801eb072297c35", "7ec967f0e37f14998082b0787b06bd",
        "7f07e7f0e47f149b0723b0787b0721", "7f0e27f1487f531b0b0bb0b6fb0722",
        "7f0e37f0e366aa89801eb072297c35", "7ec967f0e37f14998082b0723b06bd",
        "7f07e7f0e47f149b0723b0787b0721", "7f0e27f0e47f531b0723b0b6fb0722",
        "7f0e37f0e366aa89801eb072297c35", "7ec967f0e37f14998082b0723b06bd",
        "7f07e7f0e37f14998083b0787b0721", "7f0e27f0e47f531b0723b0b6fb0722",
        "7f0e37f0e366aa89801eb072297c35", "7ec967f0e37f14898082b0723b02d5",
        "7f07e7f0e37f14998082b0787b0721", "7f07e7f0e47f531b0723b0b6fb0722",
        "7f0e36665b66aa89801e9808297c35", "665f67f0e37f14898082b0723b02d5",
        "7ec967f0e37f14998082b0787b0721", "7f07e7f0e47f531b0723b0b6fb0722",
        "7f0e36665b66a449801e9808297c35", "665f67f0e37f14898082b0723b02d5",
        "7ec967f0e37f14998082b0787b06bd", "7f07e7f0e47f531b0723b0b6fb0721",
        "7f0e36665b66a449801e9808297c35", "665f67f0e37f14898082b072297c35",
        "7ec967f0e37f14998082b0787b06bd", "7f07e7f0e47f531b0723b0b6fb0721",
        "7f0e26665b66a449801e9808297c35", "665f67f0e37f1489801eb072297c35",
        "7ec967f0e37f14998082b0787b06bd", "7f07e7f0e47f531b0723b0b6fb0721",
        "7f0e27f1487f531b0b0bb0b6fb0722"
    )
    private val nStr1 = arrayListOf("日", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十")
    private val nStr2 = arrayListOf("初", "十", "廿", "卅")
    private val nStr3 = arrayListOf("正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊")

    private fun lYearDays(y: Int): Int {
        var sum = 348
        var i = 0x8000
        while (i > 0x8) {
            sum += if ((lunarInfo[y - 1900] and i) != FALSE) 1 else 0
            i = i shr 1
        }
        return (sum + leapDays(y))
    }

    private fun leapMonth(y: Int): Int {
        return lunarInfo[y - 1900] and 0xf
    }

    private fun leapDays(y: Int): Int {
        return if (leapMonth(y) != FALSE) {
            if ((lunarInfo[y - 1900] and 0x10000) != FALSE) 30 else 29
        } else 0
    }

    private fun monthDays(y: Int, m: Int): Int {
        if (m > 12 || m < 1) {
            return -1
        }
        return if (((lunarInfo[y - 1900] and (0x10000 shr m)) != FALSE)) 30 else 29
    }

    private fun solarDays(y: Int, m: Int): Int {
        if (m > 12 || m < 1) {
            return -1
        }
        val ms = m - 1
        return if (ms == 1) {
            if (((y % 4 == 0) && (y % 100 != 0) || (y % 400 == 0))) 29 else 28
        } else {
            solarMonth[ms]
        }
    }

    private fun toGanZhi(offset: Int): String {
        return gan[offset % 10] + zhi[offset % 12]
    }

    private fun getTerm(y: Int, n: Int): Int {
        if (y < 1900 || y > 2100) {
            return -1
        }
        if (n < 1 || n > 24) {
            return -1
        }
        val table = sTermInfo[y - 1900]
        val info = arrayListOf(
            table.substring(0, 5).toInt(16).toString(),
            table.substring(5, 10).toInt(16).toString(),
            table.substring(10, 15).toInt(16).toString(),
            table.substring(15, 20).toInt(16).toString(),
            table.substring(20, 25).toInt(16).toString(),
            table.substring(25, 30).toInt(16).toString()
        )
        print(info)
        val calday = arrayListOf(
            info[0].substring(0, 1), info[0].substring(1, 2), info[0].substring(3, 4),
            info[0].substring(4, 6), info[1].substring(0, 1), info[1].substring(1, 2),
            info[1].substring(3, 4), info[1].substring(4, 6), info[2].substring(0, 1),
            info[2].substring(1, 2), info[2].substring(3, 4), info[2].substring(4, 6),
            info[3].substring(0, 1), info[3].substring(1, 2), info[3].substring(3, 4),
            info[3].substring(4, 6), info[4].substring(0, 1), info[4].substring(1, 2),
            info[4].substring(3, 4), info[4].substring(4, 6), info[5].substring(0, 1),
            info[5].substring(1, 2), info[5].substring(3, 4), info[5].substring(4, 6)
        )
        return calday[n - 1].toInt()
    }

    /**
     * 公历月转农历月
     * @param m month
     * @return 1->正月，2->二月，...，10->十月，11->冬月，12->腊月
     */
    fun toChinaMonth(m: Int): String {
        if (m > 12 || m < 1) {
            return ""
        }
        var s = nStr3[m - 1]
        s += "\u6708"
        return s
    }

    /**
     * 公历日转农历日
     * @param d day
     * @return 1->初一，11->十一，21->廿一，30->卅十
     */
    fun toChinaDay(d: Int): String {
        return when (d) {
            10 -> "\u521d\u5341"
            20 -> "\u4e8c\u5341"
            30 -> "\u4e09\u5341"
            else -> {
                var s = nStr2[floor((d / 10f)).toInt()]
                s += nStr1[d % 10]
                s
            }
        }
    }

    /**
     * 获取生肖
     * @param y full year
     * @return 2021->牛
     */
    fun getAnimal(y: Int): String {
        return animals[(y - 4) % 12]
    }

    /**
     * 公历转农历
     * @param y full year : 2021
     * @param m month : 2
     * @param d day : 26
     * @return [CalendarInfo]
     */
    fun solar2lunar(y: Int, m: Int, d: Int): CalendarInfo? {
        if (y < 1900 || y > 2100) {
            return null
        }
        if (y == 1900 && m == 1 && d < 31) {
            return null
        }
        val minCalendar = Calendar.getInstance()
        minCalendar.set(1900, 0, 31)
        val objDate = Calendar.getInstance()
        objDate.set(y, m - 1, d)
        var i = 1900
        var temp = 0
        var offset = (objDate.timeInMillis - minCalendar.timeInMillis) / 86400000
        while (i < 2101 && offset > 0) {
            temp = lYearDays(i)
            offset -= temp
            i++
        }
        if (offset < 0) {
            offset += temp
            i--
        }

        val isToday = objDate.timeInMillis == Calendar.getInstance().timeInMillis
        var nWeek = objDate.get(Calendar.DAY_OF_WEEK) - 1
        val cWeek = nStr1[nWeek]
        if (nWeek == 0) {
            nWeek = 7
        }
        val year = i
        val leap = leapMonth(i)
        var isLeap = false
        i = 1
        while (i < 13 && offset > 0) {
            if (leap > 0 && i == (leap + 1) && !isLeap) {
                --i
                isLeap = true
                temp = leapDays(year)
            } else {
                temp = monthDays(year, i)
            }
            if (isLeap && i == (leap + 1)) {
                isLeap = false
            }
            offset -= temp
            i++
        }
        if (offset == 0L && leap > 0 && i == leap + 1)
            if (isLeap) {
                isLeap = false
            } else {
                isLeap = true
                --i
            }
        if (offset < 0) {
            offset += temp
            --i
        }
        val month = i
        val day = offset + 1
        val sm = m - 1
        val term3 = getTerm(year, 3)
        val gzY = if (sm < 2 && d < term3) {
            toGanZhi(year - 5)
        } else {
            toGanZhi(year - 4)
        }

        val zhiYIndex = if (sm < 2 && d < term3) {
            (year - 5) % 12 + 1
        } else {
            (year - 4) % 12 + 1
        }

        val firstNode = getTerm(y, (m * 2 - 1))
        val secondNode = getTerm(y, (m * 2))
        var gzM = toGanZhi((y - 1900) * 12 + m + 11)
        if (d >= firstNode) {
            gzM = toGanZhi((y - 1900) * 12 + m + 12)
        }
        var isTerm = false
        var term: String? = null
        if (firstNode == d) {
            isTerm = true
            term = solarTerm[m * 2 - 2]
        }
        if (secondNode == d) {
            isTerm = true
            term = solarTerm[m * 2 - 1]
        }
        val ca = Calendar.getInstance()
        ca.set(y, sm, 1)
        val dayCyclical = ca.timeInMillis / 86400000 + 25567 + 10
        val gzD = toGanZhi((dayCyclical + d - 1).toInt())
        return CalendarInfo(zhiYIndex,
            month,
            day.toInt(),
            getAnimal(year),
            (if (isLeap) "\u95f0" else "") + toChinaMonth(month),
            toChinaDay(day.toInt()),
            y, m, d,
            gzY, gzM, gzD,
            isToday, isLeap,
            nWeek, "\u661f\u671f$cWeek", isTerm, term)
    }

    /**
     * 农历转公历
     * @param y full year : 2021
     * @param m month : 1
     * @param d day : 15
     * @param isLeapMonth is leap month，是否是闰月
     * @return [CalendarInfo]
     */
    fun lunar2solar(y: Int, m: Int, d: Int, isLeapMonth: Boolean = false): CalendarInfo? {
        val leapMonth = leapMonth(y)
        if (isLeapMonth && (leapMonth != m)) {
            return null
        }
        if (y == 2100 && m == 12 && d > 1 || y == 1900 && m == 1 && d < 31) {
            return null
        }
        val day = monthDays(y, m)
        if (y < 1900 || y > 2100 || d > day) {
            return null
        }
        var offset = 0
        for (i in 1900 until y) {
            offset += lYearDays(i)
        }
        var leap: Int
        var isAdd = false
        for (i in 1 until m) {
            leap = leapMonth(y)
            if (!isAdd) {
                if (leap in 1..i) {
                    offset += leapDays(y)
                    isAdd = true
                }
            }
            offset += monthDays(y, i)
        }
        if (isLeapMonth) {
            offset += day
        }
        val stmap = Calendar.getInstance()
        stmap.set(1900, 1, 31, 0, 0, 0)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = (offset + d - 31) * 86400000L + stmap.timeInMillis
        val cY = calendar.get(Calendar.YEAR)
        val cM = calendar.get(Calendar.MONTH) + 1
        val cD = calendar.get(Calendar.DATE)
        return solar2lunar(cY, cM, cD)
    }
}

data class CalendarInfo(
    val lYear: Int,         // 农历年
    val lMonth: Int,        // 农历月
    val lDay: Int,          // 农历日
    val Animal: String,     // 生肖
    val IMonthCn: String,   // 中文农历月
    val IDayCn: String,     // 中文农历日
    val cYear: Int,         // 公历年
    val cMonth: Int,        // 公历月
    val cDay: Int,          // 公历日
    val gzYear: String,     // 干支年
    val gzMonth: String,    // 干支月
    val gzDay: String,      // 干支日
    val isToday: Boolean,   // 是否是今天
    val isLeap: Boolean,    // 是否是闰月
    val nWeek: Int,         // 当前日是一周中的第几天
    val ncWeek: String,     // 中文星期
    val isTerm: Boolean,    // 是否是节气
    val Term: String? = null// 节气
)