package exercises.ex05

import org.junit.jupiter.api.Tag
import solutions.ex05.Ex05Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class Engine
class Wheel
class Car(val engine: Engine, val wheel: Wheel)
class Garage(val car: Car, val spare: Wheel)

abstract class Ex05Spec(private val impl: Ex05) {
    @Test
    fun `05_1 dependenciesOf reads the constructor`() {
        assertEquals(listOf(Engine::class, Wheel::class), impl.dependenciesOf(Car::class))
        assertEquals(emptyList(), impl.dependenciesOf(Engine::class))
    }

    @Test
    fun `05_2 instantiate asks the provider`() {
        val engine = Engine()
        val wheel = Wheel()
        val car = impl.instantiate(Car::class) { type -> if (type == Engine::class) engine else wheel }
        assertSame(engine, car.engine)
        assertSame(wheel, car.wheel)
    }

    @Test
    fun `05_3 build wires the whole graph and reuses singletons`() {
        val cache = mutableMapOf<kotlin.reflect.KClass<*>, Any>()
        val garage = impl.build(Garage::class, cache)
        assertSame(garage.spare, garage.car.wheel, "Wheel should be built once and shared")
        assertSame(garage, impl.build(Garage::class, cache))
        assertEquals(setOf(Garage::class, Car::class, Engine::class, Wheel::class), cache.keys)
    }
}

class Ex05SolutionTest : Ex05Spec(Ex05Solution)

@Tag("exercise")
class Ex05ExerciseTest : Ex05Spec(Ex05Exercise)
