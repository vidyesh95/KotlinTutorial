package solutions.ex01

import exercises.ex01.Display
import exercises.ex01.Ex01
import exercises.ex01.VisitRepository
import exercises.ex01.VisitService
import exercises.ex01.WeatherClient
import exercises.ex01.WeatherReport

class ConsoleWeatherReport(private val client: WeatherClient, private val display: Display) : WeatherReport {
    override fun report(city: String) = display.show("$city: ${client.temperatureIn(city)}°C")
}

object Ex01Solution : Ex01 {
    override fun createWeatherReport(client: WeatherClient, display: Display): WeatherReport =
        ConsoleWeatherReport(client, display)

    override fun createServicesSharingOneRepository(): Pair<VisitService, VisitService> {
        val shared = VisitRepository()
        return VisitService(shared) to VisitService(shared)
    }
}
