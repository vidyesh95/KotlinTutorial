package exercises.ex05

import kotlin.reflect.KClass

/*
 * Exercise 05 — Reflection: autowiring by hand      (lesson: lesson05_build_a_container)
 * Replace every TODO(...) then run: ./gradlew :02-dependency-injection:exercises
 * Useful: type.primaryConstructor, constructor.parameters, parameter.type.classifier as KClass<*>,
 *         constructor.call(*args) — imports from kotlin.reflect.full.
 */
interface Ex05 {
    /** 05.1 — The classes the primary constructor of [type] needs, in parameter order. */
    fun dependenciesOf(type: KClass<*>): List<KClass<*>>

    /** 05.2 — Create [type] via its primary constructor, asking [provide] for each argument. */
    fun <T : Any> instantiate(type: KClass<T>, provide: (KClass<*>) -> Any): T

    /**
     * 05.3 — Create [type] and, recursively, everything it needs (all concrete classes).
     * Every object you build goes into [cache]; anything already in [cache] is reused (= singletons).
     */
    fun <T : Any> build(type: KClass<T>, cache: MutableMap<KClass<*>, Any>): T
}

object Ex05Exercise : Ex05 {
    override fun dependenciesOf(type: KClass<*>): List<KClass<*>> = TODO("05.1")
    override fun <T : Any> instantiate(type: KClass<T>, provide: (KClass<*>) -> Any): T = TODO("05.2")
    override fun <T : Any> build(type: KClass<T>, cache: MutableMap<KClass<*>, Any>): T = TODO("05.3")
}
