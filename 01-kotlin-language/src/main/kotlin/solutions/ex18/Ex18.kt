package solutions.ex18

import exercises.ex18.Command
import exercises.ex18.Ex18
import exercises.ex18.ParseResult
import exercises.ex18.Shape
import kotlin.math.PI

object Ex18Solution : Ex18 {
    override fun area(shape: Shape): Double = when (shape) {
        is Shape.Circle -> PI * shape.radius * shape.radius
        is Shape.Rectangle -> shape.width * shape.height
        is Shape.Triangle -> shape.base * shape.height / 2
    }

    override fun applyAll(start: Long, commands: List<Command>): Long =
        commands.fold(start) { balance, command ->
            when (command) {
                is Command.Deposit -> balance + command.amount
                is Command.Withdraw -> if (command.amount <= balance) balance - command.amount else balance
                Command.Reset -> 0
            }
        }

    override fun parseAge(text: String): ParseResult {
        val number = text.trim().toIntOrNull() ?: return ParseResult.Error("not a number")
        return if (number in 0..150) ParseResult.Ok(number) else ParseResult.Error("out of range")
    }

    override fun render(result: ParseResult): String = when (result) {
        is ParseResult.Ok -> "age=${result.value}"
        is ParseResult.Error -> "error: ${result.message}"
    }
}
