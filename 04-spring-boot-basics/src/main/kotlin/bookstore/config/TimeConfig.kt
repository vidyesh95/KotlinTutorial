package bookstore.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.time.Clock

/**
 * Time is a dependency (module 02, lesson 02). Production uses the system clock;
 * tests replace this bean with a fixed clock so timestamps are predictable.
 */
@Configuration(proxyBeanMethods = false)
class TimeConfig {
    @Bean
    fun clock(): Clock = Clock.System
}
