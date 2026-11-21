package solutions.ex02

import exercises.ex02.Ex02

object Ex02Solution : Ex02 {
    override fun secondsToClock(totalSeconds: Int): Triple<Int, Int, Int> {
        val hours = totalSeconds / 3600
        val minutes = totalSeconds % 3600 / 60
        val seconds = totalSeconds % 60
        return Triple(hours, minutes, seconds)
    }

    override fun celsiusToFahrenheit(celsius: Double): Double = celsius * 9 / 5 + 32

    override fun scoreAfter(bonus: Int): Int {
        var score = 10
        score += bonus
        score *= 2
        score -= 3
        return score
    }
}
