package exercises.ex03

/*
 * Exercise 03 — A hand-written module with lifetimes   (lesson: lesson03_composition_root)
 * Replace every TODO(...) then run: ./gradlew :02-dependency-injection:exercises
 */

data class ShopConfig(val currency: String)

class ProductRepository

class ProductService(val repository: ProductRepository, val currency: String)

class Cart(val service: ProductService) {
    val items = mutableListOf<String>()
}

interface ShopModule {
    /** Singleton, created on first access. */
    val repository: ProductRepository

    /** Singleton, created on first access, uses [repository] and the config's currency. */
    val service: ProductService

    /** Transient: a brand-new Cart (using [service]) on every call. */
    fun newCart(): Cart
}

interface Ex03 {
    /**
     * 03.1 — Build a ShopModule. Every time the module CREATES an object it must call
     * log("repository"), log("service") or log("cart") — so the test can see WHEN things are built.
     * Nothing may be created before it is first needed. Hint: `by lazy`.
     */
    fun createModule(config: ShopConfig, log: (String) -> Unit): ShopModule
}

object Ex03Exercise : Ex03 {
    override fun createModule(config: ShopConfig, log: (String) -> Unit): ShopModule = TODO("03.1")
}
