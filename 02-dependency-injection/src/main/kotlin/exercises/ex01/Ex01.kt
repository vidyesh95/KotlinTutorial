package exercises.ex01

/*
 * Exercise 01 — Dependencies from the outside      (lesson: lesson01_what_is_a_dependency)
 * Replace every TODO(...) then run: ./gradlew :02-dependency-injection:exercises
 */

interface WeatherClient {
    fun temperatureIn(city: String): Int
}

interface Display {
    fun show(text: String)
}

interface WeatherReport {
    fun report(city: String)
}

class VisitRepository {
    private var visits = 0
    fun record() { visits++ }
    fun count() = visits
}

class VisitService(private val repository: VisitRepository) {
    fun visit() = repository.record()
    fun total() = repository.count()
}

interface Ex01 {
    /**
     * 01.1 — Return a WeatherReport that asks [client] for the temperature and shows
     * "<city>: <temperature>°C" on [display]. Write a class that receives both through its constructor.
     */
    fun createWeatherReport(client: WeatherClient, display: Display): WeatherReport

    /** 01.2 — Two VisitServices (web + app) that SHARE one VisitRepository, so visits add up. */
    fun createServicesSharingOneRepository(): Pair<VisitService, VisitService>
}

object Ex01Exercise : Ex01 {
    override fun createWeatherReport(client: WeatherClient, display: Display): WeatherReport = TODO("01.1")
    override fun createServicesSharingOneRepository(): Pair<VisitService, VisitService> = TODO("01.2")
}
