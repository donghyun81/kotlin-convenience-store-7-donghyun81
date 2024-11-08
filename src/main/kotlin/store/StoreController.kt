package store

import java.io.File

class StoreController(
    private val inputView: InputView,
    private val outputView: OutputView,
) {

    fun run() {
        outputView.printStart()
        val store = createStore()
        outputView.printStoreProducts(store.getProducts())
    }

    private fun createStore(): Store = Store(getProducts(), getPromotion())

    private fun getProducts(): List<Product> {
        val productsResourcePath = "src/main/resources/products.md"
        return File(productsResourcePath).readLines().drop(1).map { product ->
            val (name, price, quantity, promotion) = product.split(",")
            Product(name, price.toInt(), quantity.toInt(), promotion.toNullOrValue())
        }
    }

    private fun getPromotion(): List<Promotion> {
        val promotionResourcePath = "src/main/resources/promotions.md"
        return File(promotionResourcePath).readLines().drop(1).map { promotion ->
            val (name, buy, get, start_date, end_date) = promotion.split(",")
            Promotion(name, buy.toInt(), get.toInt(), start_date, end_date)
        }
    }

    private fun String.toNullOrValue(): String? {
        if (this == "null") return null
        return this
    }
}