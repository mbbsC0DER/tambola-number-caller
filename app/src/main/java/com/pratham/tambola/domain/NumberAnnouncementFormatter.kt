package com.pratham.tambola.domain

object NumberAnnouncementFormatter {
    private val small = listOf("zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen")
    private val tens = listOf("", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety")

    fun whole(number: Int): String {
        require(number in 1..90)
        return if (number < 20) small[number] else tens[number / 10] +
            if (number % 10 == 0) "" else "-${small[number % 10]}"
    }

    fun live(number: Int): String {
        val whole = whole(number)
        return if (number < 10) "single number $whole" else "${small[number / 10]} ${small[number % 10]}, $whole"
    }
}
