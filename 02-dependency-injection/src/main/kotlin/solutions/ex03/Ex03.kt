package solutions.ex03

import exercises.ex03.Cart
import exercises.ex03.Ex03
import exercises.ex03.ProductRepository
import exercises.ex03.ProductService
import exercises.ex03.ShopConfig
import exercises.ex03.ShopModule

class LazyShopModule(private val config: ShopConfig, private val log: (String) -> Unit) : ShopModule {
    override val repository: ProductRepository by lazy {
        log("repository")
        ProductRepository()
    }

    override val service: ProductService by lazy {
        val repository = repository   // build dependencies first…
        log("service")                // …then this object
        ProductService(repository, config.currency)
    }

    override fun newCart(): Cart {
        val service = service
        log("cart")
        return Cart(service)
    }
}

object Ex03Solution : Ex03 {
    override fun createModule(config: ShopConfig, log: (String) -> Unit): ShopModule = LazyShopModule(config, log)
}
