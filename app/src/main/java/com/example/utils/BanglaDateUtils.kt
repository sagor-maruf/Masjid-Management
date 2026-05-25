package com.example.utils

import java.util.Calendar
import java.util.Date
import java.util.Locale

object BanglaDateUtils {

    private val banglaMonths = listOf(
        "বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন",
        "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র"
    )

    // Rough Bengali date conversion for Bangladeshi context (Revised Calendar)
    fun getBengaliDate(date: Date = Date()): String {
        val cal = Calendar.getInstance()
        cal.time = date
        val month = cal.get(Calendar.MONTH) + 1 // 1-12
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val year = cal.get(Calendar.YEAR)

        var banglaDay = 1
        var banglaMonthIndex = 0
        var banglaYear = year - 593

        // Simple calculation according to standard Bangladeshi revised calendar
        // Boishakh starts Apr 14. Joistho May 15. Ashar Jun 16. Shrabon Jul 17. Bhadra Aug 18. Ashwin Sep 19.
        // Kartik Oct 20. Agrahayan Nov 21. Poush Dec 22. Magh Jan 21. Falgun Feb 20. Chaitra Mar 21.
        when (month) {
            4 -> { // April
                if (day < 14) {
                    banglaDay = day + 17
                    banglaMonthIndex = 11 // Chaitra
                    banglaYear -= 1 // Year changes on Boishakh 1 (Apr 14)
                } else {
                    banglaDay = day - 13
                    banglaMonthIndex = 0 // Boishakh
                }
            }
            5 -> { // May
                if (day < 15) {
                    banglaDay = day + 17
                    banglaMonthIndex = 0 // Boishakh
                } else {
                    banglaDay = day - 14
                    banglaMonthIndex = 1 // Joistho
                }
            }
            6 -> { // June
                if (day < 16) {
                    banglaDay = day + 17
                    banglaMonthIndex = 1 // Joistho
                } else {
                    banglaDay = day - 15
                    banglaMonthIndex = 2 // Ashar
                }
            }
            7 -> { // July
                if (day < 17) {
                    banglaDay = day + 15
                    banglaMonthIndex = 2 // Ashar
                } else {
                    banglaDay = day - 16
                    banglaMonthIndex = 3 // Shrabon
                }
            }
            8 -> { // August
                if (day < 18) {
                    banglaDay = day + 15
                    banglaMonthIndex = 3 // Shrabon
                } else {
                    banglaDay = day - 17
                    banglaMonthIndex = 4 // Bhadra
                }
            }
            9 -> { // September
                if (day < 19) {
                    banglaDay = day + 14
                    banglaMonthIndex = 4 // Bhadra
                } else {
                    banglaDay = day - 18
                    banglaMonthIndex = 5 // Ashwin
                }
            }
            10 -> { // October
                if (day < 20) {
                    banglaDay = day + 12
                    banglaMonthIndex = 5 // Ashwin
                } else {
                    banglaDay = day - 19
                    banglaMonthIndex = 6 // Kartik
                }
            }
            11 -> { // November
                if (day < 21) {
                    banglaDay = day + 12
                    banglaMonthIndex = 6 // Kartik
                } else {
                    banglaDay = day - 20
                    banglaMonthIndex = 7 // Agrahayan
                }
            }
            12 -> { // December
                if (day < 22) {
                    banglaDay = day + 10
                    banglaMonthIndex = 7 // Agrahayan
                } else {
                    banglaDay = day - 21
                    banglaMonthIndex = 8 // Poush
                }
            }
            1 -> { // January
                if (day < 21) {
                    banglaDay = day + 10
                    banglaMonthIndex = 8 // Poush
                    banglaYear -= 1 // Still in previous Bangabda year until April
                } else {
                    banglaDay = day - 20
                    banglaMonthIndex = 9 // Magh
                    banglaYear -= 1
                }
            }
            2 -> { // February
                if (day < 20) {
                    banglaDay = day + 11
                    banglaMonthIndex = 9 // Magh
                    banglaYear -= 1
                } else {
                    banglaDay = day - 19
                    banglaMonthIndex = 10 // Falgun
                    banglaYear -= 1
                }
            }
            3 -> { // March
                if (day < 21) {
                    val isLeapYear = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
                    banglaDay = day + (if (isLeapYear) 10 else 9)
                    banglaMonthIndex = 10 // Falgun
                    banglaYear -= 1
                } else {
                    banglaDay = day - 20
                    banglaMonthIndex = 11 // Chaitra
                    banglaYear -= 1
                }
            }
        }

        return "${toBanglaNumerals(banglaDay.toString())} ${banglaMonths[banglaMonthIndex]}, ${toBanglaNumerals(banglaYear.toString())} বঙ্গাব্দ"
    }

    fun toBanglaNumerals(englishStr: String): String {
        val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val builder = java.lang.StringBuilder()
        for (char in englishStr) {
            if (char in '0'..'9') {
                builder.append(banglaDigits[char - '0'])
            } else {
                builder.append(char)
            }
        }
        return builder.toString()
    }

    fun formatBDT(value: Double): String {
        val formatted = String.format(Locale.getDefault(), "%,.2f", value)
        return "৳" + toBanglaNumerals(formatted)
    }
}
