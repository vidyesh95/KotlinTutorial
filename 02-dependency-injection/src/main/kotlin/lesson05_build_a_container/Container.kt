package lesson05_build_a_container

import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.reflect.full.primaryConstructor

/**
 * A tiny dependency-injection container (~100 lines) — the same core idea as
 * Spring's ApplicationContext, minus 20 years of features.
 *
 * Read it top to bottom; every concept used here was taught in module 01:
 *  - generics + `reified` (lessons 23–27) for the `get<T>()` / `bind<I, Impl>()` API
 *  - sealed interfaces (lesson 18) for the different kinds of bindings
 *  - lambdas with receiver (lesson 29) for the `container { ... }` DSL
 *  - KClass + reflection to read constructor parameters at runtime
 */
class Container {

    enum class Lifetime { SINGLETON, TRANSIENT }

    /** HOW to build something. */
    sealed interface Binding {
        val lifetime: Lifetime

        /** "Call this class's constructor, resolving each parameter from the container." */
        data class Constructor(val implementation: KClass<*>, override val lifetime: Lifetime) : Binding

        /** "Run this code to build it." For things we can't construct by reflection (config, lambdas…). */
        class Factory(val create: Container.() -> Any, override val lifetime: Lifetime) : Binding
    }

    private val bindings = mutableMapOf<KClass<*>, Binding>()
    private val singletons = mutableMapOf<KClass<*>, Any>()

    // ── Registration API ─────────────────────────────────────────────────────

    fun register(type: KClass<*>, binding: Binding) {
        bindings[type] = binding
    }

    /** Register a concrete class; its constructor parameters are found automatically ("autowiring"). */
    inline fun <reified T : Any> autowire(lifetime: Lifetime = Lifetime.SINGLETON) =
        register(T::class, Binding.Constructor(T::class, lifetime))

    /** "Whenever someone asks for interface I, build Impl." */
    inline fun <reified I : Any, reified Impl : I> bind(lifetime: Lifetime = Lifetime.SINGLETON) =
        register(I::class, Binding.Constructor(Impl::class, lifetime))

    /** Build it yourself, once. */
    inline fun <reified T : Any> singleton(noinline create: Container.() -> T) =
        register(T::class, Binding.Factory(create, Lifetime.SINGLETON))

    /** Build it yourself, every time. */
    inline fun <reified T : Any> factory(noinline create: Container.() -> T) =
        register(T::class, Binding.Factory(create, Lifetime.TRANSIENT))

    // ── Resolution API ───────────────────────────────────────────────────────

    inline fun <reified T : Any> get(): T = get(T::class)

    fun <T : Any> get(type: KClass<T>): T = type.cast(resolve(type, path = emptyList()))

    /**
     * The heart of the container. [path] is the chain of types we are currently
     * building, e.g. [OrderController, OrderService] — used to detect cycles and
     * to write helpful error messages.
     */
    private fun resolve(type: KClass<*>, path: List<KClass<*>>): Any {
        if (type in path) {
            val chain = (path + type).joinToString(" -> ") { it.simpleName.orEmpty() }
            throw IllegalStateException("Circular dependency: $chain")
        }
        singletons[type]?.let { return it }                    // already built? reuse it

        val binding = bindings[type] ?: run {
            val neededBy = path.lastOrNull()?.simpleName?.let { " (needed by $it)" }.orEmpty()
            throw IllegalStateException("No binding for ${type.simpleName}$neededBy")
        }

        val instance = when (binding) {
            is Binding.Factory -> binding.create(this)
            is Binding.Constructor -> construct(binding.implementation, path + type)
        }
        if (binding.lifetime == Lifetime.SINGLETON) singletons[type] = instance
        return instance
    }

    /** Reflection: read the primary constructor, resolve every parameter, call it. */
    private fun construct(implementation: KClass<*>, path: List<KClass<*>>): Any {
        val constructor = implementation.primaryConstructor
            ?: error("${implementation.simpleName} has no primary constructor")

        val arguments = constructor.parameters
            .filterNot { it.isOptional && (it.type.classifier as KClass<*>) !in bindings } // keep default values
            .associateWith { parameter -> resolve(parameter.type.classifier as KClass<*>, path) }

        return constructor.callBy(arguments)
    }
}

/** Tiny DSL: `val app = container { autowire<A>(); bind<B, BImpl>() }` */
fun container(setup: Container.() -> Unit): Container = Container().apply(setup)
