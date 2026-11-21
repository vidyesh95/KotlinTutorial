package concepts.classes_and_objects.objects

/**
 * Companion  objects : An object declaration inside a class can be marked with the companion keyword.
 *
 * Uses of companion object:
 * 1. Property needed at class level and not a specific instance.
 * 2. Factory pattern: perform extra work before an object can be used.
 */
class CompanionObjects(private val name: String) {
    private val companyId: Int = accountId++

    companion object {
        const val TAG = "Company"
        private var accountId = 1
    }

    override fun toString(): String {
        return "$TAG id $companyId name is $name"
    }
}

fun main() {
    println(CompanionObjects.TAG)
    repeat(5) {
        println(CompanionObjects("Vidyesh"))
    }
}