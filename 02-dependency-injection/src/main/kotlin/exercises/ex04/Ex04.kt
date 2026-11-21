package exercises.ex04

/*
 * Exercise 04 — What a container checks for you     (lesson: lesson04_anti_patterns)
 * Replace every TODO(...) then run: ./gradlew :02-dependency-injection:exercises
 *
 * A dependency graph maps each type name to the names of what its constructor needs:
 *   mapOf("Controller" to listOf("Service"), "Service" to listOf("Repo"), "Repo" to emptyList())
 */
interface Ex04 {
    /**
     * 04.1 — Find ANY cycle and return it as a path that starts and ends with the same node,
     * e.g. ["A", "B", "A"]. Return null when there is no cycle. Hint: depth-first search with a "path" list.
     */
    fun findCycle(graph: Map<String, List<String>>): List<String>?

    /**
     * 04.2 — An order in which to construct every key so that each type comes AFTER all of its
     * dependencies (a "topological sort"). Throw IllegalStateException if there is a cycle.
     * You may assume every dependency also appears as a key.
     */
    fun creationOrder(graph: Map<String, List<String>>): List<String>

    /** 04.3 — Dependencies that are needed but never declared as a key ("no binding for…"), sorted, no duplicates. */
    fun missingBindings(graph: Map<String, List<String>>): List<String>
}

object Ex04Exercise : Ex04 {
    override fun findCycle(graph: Map<String, List<String>>): List<String>? = TODO("04.1")
    override fun creationOrder(graph: Map<String, List<String>>): List<String> = TODO("04.2")
    override fun missingBindings(graph: Map<String, List<String>>): List<String> = TODO("04.3")
}
