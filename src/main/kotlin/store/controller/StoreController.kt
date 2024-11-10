package store.controller

import camp.nextstep.edu.missionutils.DateTimes
import store.model.*
import store.utils.retryInput
import store.utils.retryPurchase
import store.view.InputView
import store.view.OutputView
import java.io.File

class StoreController(
    private val inputView: InputView,
    private val outputView: OutputView,
) {

    fun run() {
        outputView.printStart()
        val store = createStore()
        retryPurchase {
            purchaseProducts(store)
            retryInput {
                inputView.readRetryBuy().isYes()
            }
        }
    }

    private fun purchaseProducts(store: Store) {
        outputView.printStoreProducts(store.getProducts())
        val purchaseProducts = retryInput {
            val requestedProducts = createRequestedProducts()
            createPurchaseProducts(store, requestedProducts)
        }
        outputView.printReceipt(purchaseProducts, isMemberShip())
    }

    private fun createStore(): Store = Store(getProducts(), getPromotion())

    private fun getProducts(): List<Product> {
        val productsResourcePath = "src/main/resources/products.md"
        val products = File(productsResourcePath).readLines().drop(1).map { product ->
            val (name, price, quantity, promotion) = product.split(",")
            Product(name, price.toInt(), quantity.toInt(), promotion.toNullOrValue())
        }.toMutableList()
        addNonPromotionProducts(products)
        return products.sortedSameNameByPromotion()
    }

    private fun addNonPromotionProducts(products: MutableList<Product>) {
        products.filter { product -> isNonPromotionProducts(product, products.toList()) }
            .forEach { products.add(it.copy(quantity = 0, promotion = null)) }
    }

    private fun isNonPromotionProducts(product: Product, products: List<Product>): Boolean {
        return product.promotion != null && products.find { it.name == product.name && it.promotion == null } == null
    }

    private fun getPromotion(): List<Promotion> {
        val promotionResourcePath = "src/main/resources/promotions.md"
        return File(promotionResourcePath).readLines().drop(1).map { promotion ->
            val (name, buy, get, startDate, endDate) = promotion.split(",")
            Promotion(name, buy.toInt(), get.toInt(), startDate, endDate)
        }
    }

    private fun createRequestedProducts(): List<RequestedProduct> {
        val requestedProducts = inputView.readBuyItem().split(",").map { requestedProductInput ->
            createRequestProduct(requestedProductInput)
        }
        return requestedProducts
    }

    private fun createRequestProduct(requestedProductInput: String): RequestedProduct {
        require(requestedProductInput.isNotEmpty()) { "[ERROR] 올바르지 않은 형식으로 입력했습니다. 다시 입력해 주세요." }
        require(requestedProductInput.first() == '[' && requestedProductInput.last() == ']') { "[ERROR] 올바르지 않은 형식으로 입력했습니다. 다시 입력해 주세요." }
        require(requestedProductInput.split("-").size == 2) { "[ERROR] 올바르지 않은 형식으로 입력했습니다. 다시 입력해 주세요." }
        val (name, count) = requestedProductInput.removeSurrounding("[", "]").split("-")
        requireNotNull(count.toIntOrNull()) { "[ERROR] 올바르지 않은 형식으로 입력했습니다. 다시 입력해 주세요." }
        return RequestedProduct(name, count.toInt())
    }

    private fun createPurchaseProducts(store: Store, requestedProducts: List<RequestedProduct>): List<PurchaseProduct> {
        val purchaseProducts = mutableListOf<PurchaseProduct>()
        requestedProducts.forEach { requestedProduct ->
            require(store.hasProduct(requestedProduct)) { "[ERROR] 재고 수량을 초과하여 구매할 수 없습니다. 다시 입력해 주세요." }
            purchaseProducts.add(buyRequestProduct(store, requestedProduct))
        }
        return purchaseProducts.toList()
    }

    private fun buyRequestProduct(store: Store, requestedProduct: RequestedProduct): PurchaseProduct {
        val currentDate = DateTimes.now().toLocalDate()
        if (store.isPromotion(requestedProduct, currentDate)) {
            val promotionResult = getPromotionProducts(store, requestedProduct)
            return promotionResult
        }
        return store.buyProduct(requestedProduct)
    }

    private fun getPromotionProducts(store: Store, requestedProduct: RequestedProduct): PurchaseProduct {
        val addProduct = store.calculatePromotionAppliedProduct(requestedProduct)
        if (addProduct.count > 0) return handleAddPromotionProduct(store, requestedProduct, addProduct)
        val nonPromotionProducts = store.calculateNonPromotionalProducts(requestedProduct)
        if (nonPromotionProducts.count > 0) return handleNonPromotionProducts(
            store,
            requestedProduct,
            nonPromotionProducts
        )
        return store.buyProduct(requestedProduct)
    }

    private fun handleAddPromotionProduct(
        store: Store,
        requestedProduct: RequestedProduct,
        addRequestedProduct: RequestedProduct
    ): PurchaseProduct {
        val isAddPromotionProduct = retryInput { inputView.readAddPromotionProduct(addRequestedProduct).isYes() }
        if (isAddPromotionProduct) {
            return store.buyProduct(requestedProduct.copy(count = requestedProduct.count + addRequestedProduct.count))
        }
        return store.buyProduct(requestedProduct)
    }

    private fun handleNonPromotionProducts(
        store: Store,
        requestedProduct: RequestedProduct,
        nonPromotionRequestedProduct: RequestedProduct
    ): PurchaseProduct {
        val isIncludingNonPromotions =
            retryInput { inputView.readHasNotPromotionProduct(nonPromotionRequestedProduct).isYes() }
        if (isIncludingNonPromotions) return store.buyProduct(requestedProduct)
        return store.buyProduct(
            requestedProduct.copy(count = requestedProduct.count - nonPromotionRequestedProduct.count)
        )
    }

    private fun isMemberShip() = retryInput { inputView.readIsMembershipDiscount().isYes() }


    private fun String.toNullOrValue(): String? {
        if (this == "null") return null
        return this
    }

    private fun String.isYes(): Boolean {
        require(this.uppercase() == "Y" || this.uppercase() == "N") { "[ERROR] 출력 형식이 맞지 않습니다 Y 또는 N만 입력해주세요" }
        return this.uppercase() == "Y"
    }

    private fun List<Product>.sortedSameNameByPromotion() =
        groupBy { it.name }.flatMap { (_, group) -> group.sortedByDescending { it.promotion } }.toList()
}