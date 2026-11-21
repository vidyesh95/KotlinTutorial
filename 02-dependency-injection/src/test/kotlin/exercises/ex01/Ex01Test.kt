package exercises.ex01

import org.junit.jupiter.api.Tag
import solutions.ex01.Ex01Solution
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class Ex01Spec(private val impl: Ex01) {
    @Test
    fun `01_1 report uses the injected client and display`() {
        val shown = mutableListOf<String>()
        val fakeClient = object : WeatherClient {
            override fun temperatureIn(city: String) = if (city == "Oslo") -3 else 25
        }
        val fakeDisplay = object : Display {
            override fun show(text: String) { shown += text }
        }
        val report = impl.createWeatherReport(fakeClient, fakeDisplay)
        report.report("Oslo")
        report.report("Rome")
        assertEquals(listOf("Oslo: -3°C", "Rome: 25°C"), shown)
    }

    @Test
    fun `01_2 services share one repository`() {
        val (web, app) = impl.createServicesSharingOneRepository()
        web.visit()
        app.visit()
        app.visit()
        assertEquals(3, web.total())
        assertEquals(3, app.total())
    }
}

class Ex01SolutionTest : Ex01Spec(Ex01Solution)

@Tag("exercise")
class Ex01ExerciseTest : Ex01Spec(Ex01Exercise)
