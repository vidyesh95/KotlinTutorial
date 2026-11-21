package solutions.ex12

import exercises.ex12.Discount
import exercises.ex12.Ex12

object Ex12Solution : Ex12 {
    override fun applyTwice(f: (Int) -> Int, x: Int): Int = f(f(x))

    override fun makeAdder(n: Int): (Int) -> Int = { it + n }

    override fun counter(): () -> Int {
        var count = 0
        return { ++count }
    }

    override fun percentOff(percent: Int): Discount = Discount { price -> price * (100 - percent) / 100 }
}
