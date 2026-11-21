package solutions.ex16

import exercises.ex16.Employee
import exercises.ex16.Ex16
import exercises.ex16.Greeter
import exercises.ex16.Walker

object Ex16Solution : Ex16 {
    override fun salaried(name: String, yearlySalary: Int): Employee = SalariedEmployee(name, yearlySalary)
    override fun hourly(name: String, hours: Int, rate: Int): Employee = HourlyEmployee(name, hours, rate)
    override fun payroll(staff: List<Employee>): Int = staff.sumOf { it.monthlyPay() }
    override fun robot(name: String): Greeter = Robot(name)
}

class SalariedEmployee(name: String, private val yearlySalary: Int) : Employee(name) {
    override fun monthlyPay() = yearlySalary / 12
}

class HourlyEmployee(name: String, private val hours: Int, private val rate: Int) : Employee(name) {
    override fun monthlyPay(): Int {
        val regular = minOf(hours, 160) * rate
        val overtime = maxOf(hours - 160, 0) * rate * 3 / 2
        return regular + overtime
    }
}

class Robot(override val name: String) : Greeter, Walker {
    override fun walk(steps: Int) = "$name walked $steps steps"
}
