package solutions.ex04

import exercises.ex04.Ex04

object Ex04Solution : Ex04 {
    override fun findCycle(graph: Map<String, List<String>>): List<String>? {
        val done = mutableSetOf<String>()

        fun visit(node: String, path: List<String>): List<String>? {
            if (node in path) return path.drop(path.indexOf(node)) + node   // back at a node on our path
            if (node in done) return null
            for (dependency in graph[node].orEmpty()) {
                visit(dependency, path + node)?.let { return it }
            }
            done += node
            return null
        }

        for (node in graph.keys) visit(node, emptyList())?.let { return it }
        return null
    }

    override fun creationOrder(graph: Map<String, List<String>>): List<String> {
        findCycle(graph)?.let { error("Circular dependency: ${it.joinToString(" -> ")}") }
        val order = mutableListOf<String>()

        fun visit(node: String) {
            if (node in order) return
            graph[node].orEmpty().forEach(::visit)   // dependencies first…
            order += node                            // …then the node itself
        }

        graph.keys.forEach(::visit)
        return order
    }

    override fun missingBindings(graph: Map<String, List<String>>): List<String> =
        graph.values.flatten().filter { it !in graph }.distinct().sorted()
}
