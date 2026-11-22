package exercises.ex04

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/*
 * Exercise 04 — Configuration                   (lesson: lessons/04-configuration.md)
 * Run: ./gradlew :04-spring-boot-basics:exercises --tests 'exercises.ex04*'
 *
 * application.yml contains:
 *   exercises:
 *     shipping:
 *       flat-fee-cents: 499
 *       free-above-cents: 3000
 * The test overrides free-above-cents to 5000 — so your code must READ the config, not hard-code it.
 */

/** Bound from `exercises.shipping.*` (shared by exercise and solution). */
@ConfigurationProperties(prefix = "exercises.shipping")
data class ShippingProperties(
    val flatFeeCents: Int,
    val freeAboveCents: Int,
)

data class ShippingQuote(val orderTotalCents: Int, val shippingCents: Int, val freeAboveCents: Int)

data class Ex04Greeting(val text: String)

@RestController
@RequestMapping("/exercises/ex04")
class Ex04ShippingController(
    private val shipping: ShippingProperties,
    @param:Value("\${bookstore.name}") private val shopName: String,   // @Value: inject ONE property
) {
    /**
     * 04.1 — GET /exercises/ex04/shipping?orderTotalCents=4000
     * → {"orderTotalCents": 4000, "shippingCents": …, "freeAboveCents": …}
     * Shipping is free (0) when orderTotalCents >= freeAboveCents, otherwise flatFeeCents.
     */
    @GetMapping("/shipping")
    fun quote(@RequestParam orderTotalCents: Int): ShippingQuote = TODO("04.1")

    /** 04.2 — GET /exercises/ex04/greeting → {"text": "Welcome to <bookstore.name>"} using the injected shopName. */
    @GetMapping("/greeting")
    fun greeting(): Ex04Greeting = TODO("04.2")
}
