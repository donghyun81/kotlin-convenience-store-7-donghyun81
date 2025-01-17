package store.controller

import camp.nextstep.edu.missionutils.DateTimes
import store.common.ZERO
import store.common.enum.ErrorMessage
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
                inputView.confirmAdditionalPurchase().isAccept()
            }
        }
    }

    private fun purchaseProducts(store: Store) {
        outputView.printStoreProducts(store.getProducts())
        val purchaseProducts = retryInput {
            val requestedProducts = createRequestedProducts(store)
            createPurchaseProducts(store, requestedProducts)
        }
        outputView.printReceipt(purchaseProducts, isMemberShip())
    }

    private fun createStore(): Store = Store(getProducts(), getPromotion())

    private fun getProducts(): List<Product> {
        val productsResourcePath = PRODUCT_PATH
        val products = File(productsResourcePath).readLines().drop(ATTRIBUTES_LINE).map { product ->
            val (name, price, quantity, promotion) = product.split(DELIMITER_COMMA)
            Product(name, price.toInt(), quantity.toInt(), promotion.toNullOrValue())
        }.toMutableList()
        addNonPromotionProducts(products)
        return products.sortedSameNameByPromotion()
    }

    private fun addNonPromotionProducts(products: MutableList<Product>) {
        products.filter { product -> isNonPromotionProducts(product, products.toList()) }
            .forEach { products.add(it.copy(quantity = ZERO, promotion = null)) }
    }

    private fun isNonPromotionProducts(product: Product, products: List<Product>): Boolean {
        return product.promotion != null && products.find { it.name == product.name && it.promotion == null } == null
    }

    private fun getPromotion(): List<Promotion> {
        val promotionResourcePath = PROMOTION_PATH
        return File(promotionResourcePath).readLines().drop(ATTRIBUTES_LINE).map { promotion ->
            val (name, buy, get, startDate, endDate) = promotion.split(DELIMITER_COMMA)
            Promotion(name, buy.toInt(), get.toInt(), startDate, endDate)
        }
    }

    private fun createRequestedProducts(store: Store): List<RequestedProduct> {
        val requestedProducts = inputView.readPurchaseInput().split(DELIMITER_COMMA).map { requestedProductInput ->
            val requestedProduct = createRequestProduct(requestedProductInput, store.getProducts())
            requestedProduct
        }.mergeDuplicateProducts()
        validateRequestProducts(requestedProducts, store)
        return requestedProducts
    }

    private fun validateRequestProducts(requestedProducts: List<RequestedProduct>, store: Store) {
        requestedProducts.map { requestedProduct ->
            require(store.hasProduct(requestedProduct)) { ErrorMessage.STOCK_EXCEEDED.getErrorMessage() }
        }
    }

    private fun createRequestProduct(requestedProductInput: String, products: List<Product>): RequestedProduct {
        validateRequestProductInput(requestedProductInput)
        val (name, count) = requestedProductInput.removeSurrounding(
            PURCHASE_PRODUCT_PREFIX.toString(),
            PURCHASE_PRODUCT_SUFFIX.toString()
        ).split(DELIMITER_HYPHEN)
        requireNotNull(products.find { it.name == name }) { ErrorMessage.NON_EXISTENT_PRODUCT.getErrorMessage() }
        requireNotNull(count.toIntOrNull()) { ErrorMessage.INVALID_INPUT.getErrorMessage() }
        return RequestedProduct(name, count.toInt())
    }

    private fun validateRequestProductInput(requestedProductInput: String) {
        require(requestedProductInput.isNotEmpty()) { ErrorMessage.INVALID_INPUT.getErrorMessage() }
        require(requestedProductInput.first() == PURCHASE_PRODUCT_PREFIX && requestedProductInput.last() == PURCHASE_PRODUCT_SUFFIX) {
            ErrorMessage.INVALID_INPUT.getErrorMessage()
        }
        require(requestedProductInput.split(DELIMITER_HYPHEN).size == PURCHASE_PRODUCT_FORM_SIZE) {
            ErrorMessage.INVALID_INPUT.getErrorMessage()
        }
    }

    private fun createPurchaseProducts(store: Store, requestedProducts: List<RequestedProduct>): List<PurchaseProduct> {
        val purchaseProducts = mutableListOf<PurchaseProduct>()
        requestedProducts.forEach { requestedProduct ->
            purchaseProducts.add(buyRequestProduct(store, requestedProduct))
        }
        return purchaseProducts.toList()
    }

    private fun buyRequestProduct(store: Store, requestedProduct: RequestedProduct): PurchaseProduct {
        val currentDate = DateTimes.now().toLocalDate()
        if (store.isPromotion(requestedProduct, currentDate)) {
            val promotionResult = getPromotionProduct(store, requestedProduct)
            return promotionResult
        }
        return store.buyProduct(requestedProduct)
    }

    private fun getPromotionProduct(store: Store, requestedProduct: RequestedProduct): PurchaseProduct {
        val addProduct = store.calculatePromotionAppliedProduct(requestedProduct)
        if (addProduct.count > ZERO) return handleAddPromotionProduct(store, requestedProduct, addProduct)
        val nonPromotionProducts = store.calculateNonPromotionalProducts(requestedProduct)
        return handleNonPromotionProduct(store, requestedProduct, nonPromotionProducts)
    }

    private fun handleAddPromotionProduct(
        store: Store, product: RequestedProduct, addProduct: RequestedProduct
    ): PurchaseProduct {
        val isAddPromotionProduct = retryInput { inputView.confirmPromotionAddition(addProduct).isAccept() }
        if (isAddPromotionProduct) {
            return store.buyProduct(product.copy(count = product.count + addProduct.count))
        }
        return store.buyProduct(product)
    }

    private fun handleNonPromotionProduct(
        store: Store, product: RequestedProduct, nonPromotionProduct: RequestedProduct
    ): PurchaseProduct {
        if (nonPromotionProduct.count <= ZERO) return store.buyProduct(product)
        val isIncludingNonPromotions =
            retryInput { inputView.confirmNonPromotionalPurchase(nonPromotionProduct).isAccept() }
        if (isIncludingNonPromotions) return store.buyProduct(product)
        return store.buyProduct(product.copy(count = product.count - nonPromotionProduct.count))
    }

    private fun isMemberShip() = retryInput { inputView.confirmMembershipDiscount().isAccept() }


    private fun String.toNullOrValue(): String? {
        if (this == NULL_TEXT) return null
        return this
    }

    private fun String.isAccept(): Boolean {
        require(this == INPUT_YES || this.uppercase() == INPUT_NO) { ErrorMessage.INVALID_FORMAT.getErrorMessage() }
        return this.uppercase() == INPUT_YES
    }

    private fun List<Product>.sortedSameNameByPromotion() =
        groupBy { it.name }.flatMap { (_, group) -> group.sortedByDescending { it.promotion } }.toList()

    private fun List<RequestedProduct>.mergeDuplicateProducts() = groupBy { it.name }
        .map { (name, requestProducts) -> RequestedProduct(name, requestProducts.sumOf { it.count }) }.toList()

    companion object {
        private const val DELIMITER_COMMA = ","
        private const val DELIMITER_HYPHEN = "-"
        private const val ATTRIBUTES_LINE = 1
        private const val NULL_TEXT = "null"
        private const val INPUT_YES = "Y"
        private const val INPUT_NO = "N"
        private const val PURCHASE_PRODUCT_PREFIX = '['
        private const val PURCHASE_PRODUCT_SUFFIX = ']'
        private const val PURCHASE_PRODUCT_FORM_SIZE = 2
        private const val PRODUCT_PATH = "src/main/resources/products.md"
        private const val PROMOTION_PATH = "src/main/resources/promotions.md"
    }
}