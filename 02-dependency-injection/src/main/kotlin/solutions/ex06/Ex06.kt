package solutions.ex06

import exercises.ex06.EnglishGreeter
import exercises.ex06.Ex06
import exercises.ex06.Greeter
import exercises.ex06.GreetingService
import exercises.ex06.SpanishGreeter
import exercises.ex06.VisitCounter
import org.springframework.beans.factory.getBean
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Scope

// The exercise classes have no annotations, so we register them with @Bean methods.

@Configuration(proxyBeanMethods = false)
class EnglishConfig {
    @Bean fun greeter(): Greeter = EnglishGreeter()
    @Bean fun greetingService(greeter: Greeter) = GreetingService(greeter)
}

@Configuration(proxyBeanMethods = false)
class BilingualConfig {
    @Bean fun english(): Greeter = EnglishGreeter()
    @Bean @Primary fun spanish(): Greeter = SpanishGreeter()
    @Bean fun greetingService(greeter: Greeter) = GreetingService(greeter)
}

@Configuration(proxyBeanMethods = false)
class CounterConfig {
    @Bean @Scope("prototype") fun visitCounter() = VisitCounter()
}

object Ex06Solution : Ex06 {
    override fun welcomeWithSpring(name: String): String =
        AnnotationConfigApplicationContext(EnglishConfig::class.java).use { it.getBean<GreetingService>().welcome(name) }

    override fun welcomeWithPrimary(name: String): String =
        AnnotationConfigApplicationContext(BilingualConfig::class.java).use { it.getBean<GreetingService>().welcome(name) }

    override fun prototypeGivesNewInstances(): Boolean =
        AnnotationConfigApplicationContext(CounterConfig::class.java).use {
            it.getBean<VisitCounter>() !== it.getBean<VisitCounter>()
        }
}
