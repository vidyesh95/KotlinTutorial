package solutions.ex27

inline fun <reified T> firstOfType(items: List<Any?>): T? = items.firstOrNull { it is T } as T?

inline fun <reified T> countOfType(items: List<Any?>): Int = items.count { it is T }

inline fun <reified K, reified V> isMapOf(value: Any?): Boolean =
    value is Map<*, *> && value.all { (key, v) -> key is K && v is V }

fun <T> filterByClass(items: List<Any?>, type: Class<T>): List<T> =
    items.filter(type::isInstance).map(type::cast)
