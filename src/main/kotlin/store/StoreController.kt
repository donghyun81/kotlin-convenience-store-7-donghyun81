package store

import camp.nextstep.edu.missionutils.DateTimes
import java.io.File

class StoreController(
    private val inputView: InputView,
    private val outputView: OutputView,
) {

    fun run() {
        outputView.printStart()
        val store = createStore()
        retryBuy {
            outputView.printStoreProducts(store.getProducts())
            val purchaseProducts = retryInput {
                val requestedProducts = createRequestedProducts()
                createPurchaseProducts(store, requestedProducts)
            }
            outputView.printReceipt(purchaseProducts, isMemberShip())
            inputView.readRetryBuy() == "Y"
        }
    }

    private fun createStore(): Store = Store(getProducts(), getPromotion())

    private fun getProducts(): List<Product> {
        val productsResourcePath = "src/main/resources/products.md"
        val products = File(productsResourcePath).readLines().drop(1).map { product ->
            val (name, price, quantity, promotion) = product.split(",")
            Product(name, price.toInt(), quantity.toInt(), promotion.toNullOrValue())
        }.toMutableList()
        addNonPromotionProducts(products)
        return products.groupBy { it.name }.flatMap { (_, group) -> group.sortedByDescending { it.promotion } }.toList()
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
            val (name, buy, get, start_date, end_date) = promotion.split(",")
            Promotion(name, buy.toInt(), get.toInt(), start_date, end_date)
        }
    }

    private fun createRequestedProducts(): List<RequestedProduct> {
        val requestedProducts = inputView.readBuyItem().split(",").map { requestedProductInput ->
            createRequestProduct(requestedProductInput)
        }
        return requestedProducts
    }

    private fun createRequestProduct(requestedProductInput: String): RequestedProduct {
        require(requestedProductInput.first() == '[' && requestedProductInput.last() == ']') { "[ERROR] 올바르지 않은 형식으로 입력했습니다. 다시 입력해 주세요." }
        val (name, count) = requestedProductInput.removeSurrounding("[", "]").split("-")
        return RequestedProduct(name, count.toInt())
    }

    private fun createPurchaseProducts(store: Store, requestedProducts: List<RequestedProduct>): List<PurchaseProduct> {
        val purchaseProducts = mutableListOf<PurchaseProduct>()
        requestedProducts.forEach { requestedProduct ->
            require(store.hasProduct(requestedProduct)) { "[ERROR] 재고 수량을 초과하여 구매할 수 없습니다. 다시 입력해 주세요." }
            purchaseProducts.add(buyRequestProducts(store, requestedProduct))
        }
        return purchaseProducts.toList()
    }

    private fun buyRequestProducts(store: Store, requestedProduct: RequestedProduct): PurchaseProduct {
        store.hasProduct(requestedProduct)
        val currentDate = DateTimes.now().toLocalDate()
        if (store.isPromotion(requestedProduct, currentDate)) {
            val promotionResult = getPromotionProducts(store, requestedProduct)
            return promotionResult
        }
        return store.buyProduct(requestedProduct)
    }

    private fun getPromotionProducts(store: Store, requestedProduct: RequestedProduct): PurchaseProduct {
        val addProduct = store.getApplyPromotionProduct(requestedProduct)
        if (addProduct.count > 0) return handleAddPromotionProduct(store, requestedProduct, addProduct)

        val nonPromotionProducts = store.getNonPromotionalProducts(requestedProduct)
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
        addProduct: RequestedProduct
    ): PurchaseProduct {
        val yesOrNo = inputView.readAddPromotionProduct(addProduct)
        if (yesOrNo == "Y") {
            val totalCount = requestedProduct.count + addProduct.count
            return store.buyProduct(requestedProduct.copy(count = totalCount))
        }
        return store.buyProduct(requestedProduct)
    }

    private fun handleNonPromotionProducts(
        store: Store,
        requestedProduct: RequestedProduct,
        nonPromotionProducts: RequestedProduct
    ): PurchaseProduct {
        val yesOrNo = inputView.readHasNotPromotionProduct(nonPromotionProducts)
        if (yesOrNo == "Y") return store.buyProduct(requestedProduct)
        return store.buyProduct(
            requestedProduct.copy(count = requestedProduct.count - nonPromotionProducts.count)
        )
    }

    private fun isMemberShip() = inputView.readIsMembershipDiscount() == "Y"

    private fun String.toNullOrValue(): String? {
        if (this == "null") return null
        return this
    }
}