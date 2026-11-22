package solutions.ex04

import exercises.ex04.Ex04Greeting
import exercises.ex04.ShippingProperties
import exercises.ex04.ShippingQuote
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/solutions/ex04")
class Ex04ShippingSolutionController(
    private val shipping: ShippingProperties,
    @param:Value("\${bookstore.name}") private val shopName: String,
) {
    @GetMapping("/shipping")
    fun quote(@RequestParam orderTotalCents: Int): ShippingQuote {
        val fee = if (orderTotalCents >= shipping.freeAboveCents) 0 else shipping.flatFeeCents
        return ShippingQuote(orderTotalCents, fee, shipping.freeAboveCents)
    }

    @GetMapping("/greeting")
    fun greeting() = Ex04Greeting("Welcome to $shopName")
}
