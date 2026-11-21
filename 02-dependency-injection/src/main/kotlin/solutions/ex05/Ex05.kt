package solutions.ex05

import exercises.ex05.Ex05
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.reflect.full.primaryConstructor

object Ex05Solution : Ex05 {
    override fun dependenciesOf(type: KClass<*>): List<KClass<*>> =
        type.primaryConstructor?.parameters.orEmpty().map { it.type.classifier as KClass<*> }

    override fun <T : Any> instantiate(type: KClass<T>, provide: (KClass<*>) -> Any): T {
        val constructor = requireNotNull(type.primaryConstructor) { "${type.simpleName} has no primary constructor" }
        val arguments = dependenciesOf(type).map(provide)
        return constructor.call(*arguments.toTypedArray())
    }

    override fun <T : Any> build(type: KClass<T>, cache: MutableMap<KClass<*>, Any>): T {
        cache[type]?.let { return type.cast(it) }
        val instance = instantiate(type) { dependency -> build(dependency, cache) }
        cache[type] = instance
        return instance
    }
}
