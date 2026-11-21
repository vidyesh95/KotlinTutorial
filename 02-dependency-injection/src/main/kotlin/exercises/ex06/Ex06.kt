package exercises.ex06

/*
 * Exercise 06 — Spring's container                  (lesson: lesson06_spring_container)
 * Replace every TODO(...) then run: ./gradlew :02-dependency-injection:exercises
 *
 * The classes below have NO Spring annotations yet. Add what's needed (@Component, @Primary,
 * @Scope…) or write a @Configuration class with @Bean methods — your choice.
 */

interface Greeter {
    fun greet(name: String): String
}

class EnglishGreeter : Greeter {
    override fun greet(name: String) = "Hello, $name!"
}

class SpanishGreeter : Greeter {
    override fun greet(name: String) = "Hola, $name!"
}

class GreetingService(private val greeter: Greeter) {
    fun welcome(name: String) = greeter.greet(name)
}

class VisitCounter {
    var visits = 0
}

interface Ex06 {
    /** 06.1 — Create a context with EnglishGreeter + GreetingService, return getBean<GreetingService>().welcome(name). */
    fun welcomeWithSpring(name: String): String

    /**
     * 06.2 — Create a context with BOTH greeters + GreetingService. Make SpanishGreeter the default,
     * so welcome("Ada") returns "Hola, Ada!".
     */
    fun welcomeWithPrimary(name: String): String

    /** 06.3 — Register VisitCounter in PROTOTYPE scope; return true if two getBean calls give different objects. */
    fun prototypeGivesNewInstances(): Boolean
}

object Ex06Exercise : Ex06 {
    override fun welcomeWithSpring(name: String): String = TODO("06.1")
    override fun welcomeWithPrimary(name: String): String = TODO("06.2")
    override fun prototypeGivesNewInstances(): Boolean = TODO("06.3")
}
