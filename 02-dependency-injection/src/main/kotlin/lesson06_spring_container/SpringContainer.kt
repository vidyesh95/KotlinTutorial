/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  DI Lesson 06 — The real thing: Spring's IoC container
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : do with Spring everything we did by hand — components, @Bean,
 *         multiple implementations, scopes, lifecycle — before Spring Boot
 *         hides the setup in module 04.
 *  Needs: lesson 05 (you built a mini Spring there!)
 *  Time : ~40 min
 *  Next : module 03 — SQL fundamentals
 *  Exercises: exercises/ex06/Ex06.kt
 */
package lesson06_spring_container

import org.springframework.beans.factory.NoUniqueBeanDefinitionException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.getBean
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import kotlin.time.Instant

fun main() {
    section1ComponentsAndConstructorInjection()
    section2BeansFromConfiguration()
    section3SeveralImplementations()
    section4Scopes()
    section5Lifecycle()
}

// Vocabulary:
//  • Bean               = an object created and managed by Spring (our "registered thing").
//  • ApplicationContext = Spring's container (our `Container` from lesson 05).
//  • @Component         = "Spring, please manage this class" (our `autowire<T>()`).

interface Notifier {
    fun send(message: String)
}

fun interface TimeSource {
    fun now(): Instant
}

// ───────────────────────────────────────────────────────────────────────────
// §1. @Component + constructor injection
// ───────────────────────────────────────────────────────────────────────────
// Annotate classes with @Component, hand them to the context, ask for one.
// Spring reads constructors (like our container) and wires everything.
// One constructor → no @Autowired needed.
//
// 🧒 Like I'm 5:
//    Lesson 05's LEGO robot, but it's a whole factory with 20 years of
//    experience. You just put a sticker (@Component) on each box saying
//    "the robot may build this", and the factory does the rest.

@Component
class BookRepository {
    private val books = mutableListOf<String>()
    fun add(title: String) { books += title }
    fun count() = books.size
}

@Component
class ConsoleNotifier : Notifier {
    override fun send(message: String) = println("  [console] $message")
}

@Component
class BookService(private val repository: BookRepository, private val notifier: Notifier) {
    fun addBook(title: String) {
        repository.add(title)
        notifier.send("added $title (${repository.count()} books)")
    }
}

fun section1ComponentsAndConstructorInjection() {
    println("§1")
    // AnnotationConfigApplicationContext(...) = create + register + build all singletons ("refresh").
    // `.use { }` closes the context at the end (it's AutoCloseable, like a file).
    AnnotationConfigApplicationContext(
        BookRepository::class.java,
        ConsoleNotifier::class.java,
        BookService::class.java,
    ).use { context ->
        val service = context.getBean<BookService>()   // Kotlin extension with `reified T` (module 01, lesson 27!)
        service.addBook("Dune")
        println("beans: ${context.beanDefinitionNames.filter { !it.startsWith("org.springframework") }}")
    }
}
// 🏗️ In Spring Boot you won't list classes: @SpringBootApplication turns on
// *component scanning* — Spring searches your packages for @Component (and its
// cousins @Service, @Repository, @Controller, @RestController).

// ───────────────────────────────────────────────────────────────────────────
// §2. @Configuration + @Bean: for things you can't (or don't want to) annotate
// ───────────────────────────────────────────────────────────────────────────
// Library classes, lambdas, objects needing config values: write a function
// that builds it and mark it @Bean. Its parameters are injected too.
// (= our `singleton { ... }` from lesson 05.)

class ClockedGreeter(private val time: TimeSource, private val shopName: String) {
    fun greet(name: String) = "Welcome to $shopName, $name! It is ${time.now()}"
}

@Configuration(proxyBeanMethods = false)   // plain factory methods; see note below
class ShopConfiguration {
    @Bean
    fun timeSource(): TimeSource = TimeSource { Instant.parse("2026-03-01T08:00:00Z") }

    @Bean
    fun greeter(time: TimeSource): ClockedGreeter = ClockedGreeter(time, shopName = "Kotlin Books")
}

fun section2BeansFromConfiguration() {
    println("§2")
    AnnotationConfigApplicationContext(ShopConfiguration::class.java).use { context ->
        println(context.getBean<ClockedGreeter>().greet("ada"))
    }
}
// ⚠️ Gotcha: Kotlin classes are `final` by default, but Spring sometimes needs
// to SUBCLASS your classes to add behaviour (proxies). With
// `proxyBeanMethods = false` no subclass is needed. In Spring Boot modules we'll
// add the `kotlin("plugin.spring")` compiler plugin, which opens Spring-annotated
// classes automatically.

// ───────────────────────────────────────────────────────────────────────────
// §3. Two implementations of one interface
// ───────────────────────────────────────────────────────────────────────────
// Ask for a Notifier when there are TWO? Spring can't guess → startup error.
// Fix with @Primary (the default choice) or @Qualifier (ask for one by name).

@Component("email")
class EmailNotifier : Notifier {
    override fun send(message: String) = println("  [email] $message")
}

@Component("sms")
class SmsNotifier : Notifier {
    override fun send(message: String) = println("  [sms] $message")
}

@Component
@Primary
class DefaultEmailNotifier : Notifier {          // the default when nobody says otherwise
    override fun send(message: String) = println("  [email, primary] $message")
}

@Component
class AlertService(
    private val notifier: Notifier,                          // gets the @Primary one
    @param:Qualifier("sms") private val urgent: Notifier,    // asks for the "sms" bean by name
) {
    fun alert(message: String) {
        notifier.send(message)
        urgent.send("URGENT: $message")
    }
}

fun section3SeveralImplementations() {
    println("§3")
    // Without @Primary: Spring refuses because it can't choose.
    AnnotationConfigApplicationContext(EmailNotifier::class.java, SmsNotifier::class.java).use { context ->
        val error = runCatching { context.getBean<Notifier>() }.exceptionOrNull()
        println("ambiguous: ${error is NoUniqueBeanDefinitionException}")
    }
    AnnotationConfigApplicationContext(
        EmailNotifier::class.java, SmsNotifier::class.java,
        DefaultEmailNotifier::class.java, AlertService::class.java,
    ).use { context ->
        context.getBean<AlertService>().alert("disk almost full")
    }
}
// Why `@param:Qualifier`? An annotation on a constructor `val` could belong to
// the parameter, the property or the field. `@param:` says "the parameter",
// which is where Spring looks. (🆕 Kotlin 2.4 made the default rules for this
// stable, but being explicit keeps it obvious.)

// ───────────────────────────────────────────────────────────────────────────
// §4. Scopes: singleton (default) vs prototype
// ───────────────────────────────────────────────────────────────────────────

@Component
class Catalog              // singleton: one per context

@Component
@Scope("prototype")
class ShoppingCart         // prototype: new instance on every getBean / injection

fun section4Scopes() {
    println("§4")
    AnnotationConfigApplicationContext(Catalog::class.java, ShoppingCart::class.java).use { context ->
        println("catalog shared: ${context.getBean<Catalog>() === context.getBean<Catalog>()}")
        println("cart shared:    ${context.getBean<ShoppingCart>() === context.getBean<ShoppingCart>()}")
    }
}
// Web apps add "request" and "session" scopes (lesson 03's scoped lifetime).

// ───────────────────────────────────────────────────────────────────────────
// §5. Lifecycle: run code after creation and before shutdown
// ───────────────────────────────────────────────────────────────────────────
// Connection pools open at startup and close at shutdown. Spring calls the
// methods you name once the bean is fully built, and again when the context
// closes — in REVERSE creation order, so nothing is closed while still in use.

class ConnectionPool(private val name: String) {
    fun start() = println("  $name: open connections")
    fun stop() = println("  $name: close connections")
}

class ReportJob(private val pool: ConnectionPool) {
    fun start() = println("  report job: scheduled (uses pool)")
    fun stop() = println("  report job: cancelled")
    fun run() = println("  report job: running")
}

@Configuration(proxyBeanMethods = false)
class LifecycleConfiguration {
    @Bean(initMethod = "start", destroyMethod = "stop")
    fun pool() = ConnectionPool("db-pool")

    @Bean(initMethod = "start", destroyMethod = "stop")
    fun reportJob(pool: ConnectionPool) = ReportJob(pool)
}

fun section5Lifecycle() {
    println("§5")
    println("starting context…")
    AnnotationConfigApplicationContext(LifecycleConfiguration::class.java).use { context ->
        context.getBean<ReportJob>().run()
        println("closing context…")
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • ApplicationContext = the container; beans = objects it manages.
//  • @Component on your classes, @Bean methods for everything else.
//  • Constructor injection, no @Autowired needed for a single constructor.
//  • Several candidates → @Primary or @Qualifier. Scopes: singleton / prototype.
//  • initMethod / destroyMethod (or @PostConstruct / @PreDestroy) for lifecycle.
//  Module 04 adds Spring Boot: scanning, auto-configuration, web server, config files.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
//   [console] added Dune (1 books)
// beans: [bookRepository, consoleNotifier, bookService]
// §2
// Welcome to Kotlin Books, ada! It is 2026-03-01T08:00:00Z
// §3
// ambiguous: true
//   [email, primary] disk almost full
//   [sms] URGENT: disk almost full
// §4
// catalog shared: true
// cart shared:    false
// §5
// starting context…
//   db-pool: open connections
//   report job: scheduled (uses pool)
//   report job: running
// closing context…
//   report job: cancelled
//   db-pool: close connections
