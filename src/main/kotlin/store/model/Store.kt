package store.model

import camp.nextstep.edu.missionutils.DateTimes
import store.common.enum.ErrorMessage
import java.time.LocalDate

class Store(
    private val products: List<Product>,
    private val promotions: List<Promotion>
) {
    fun getProducts() = products.toList()

    fun hasProduct(requestedProduct: RequestedProduct): Boolean {
        var remainingRequestCount = requestedProduct.count
        products.map { product ->
            if (product.name == requestedProduct.name) remainingRequestCount -= product.getQuantity()
        }
        return remainingRequestCount <= ZERO
    }

    fun isPromotion(requestedProduct: RequestedProduct, currentDate: LocalDate): Boolean {
        val product = products.find { it.name == requestedProduct.name } ?: return false
        val promotion = promotions.find { it.name == product.promotion } ?: return false
        return isPromotionPeriod(promotion, currentDate)
    }

    private fun isPromotionPeriod(promotion: Promotion, currentDate: LocalDate): Boolean {
        val startDate = LocalDate.parse(promotion.startDate)
        val endDate = LocalDate.parse(promotion.endDate)
        return currentDate in startDate..endDate
    }

    fun calculatePromotionAppliedProduct(requestedProduct: RequestedProduct): RequestedProduct {
        val product = products.find { it.name == requestedProduct.name } ?: throw IllegalArgumentException()
        val promotion = promotions.find { it.name == product.promotion } ?: throw IllegalArgumentException()
        if (requestedProduct.count >= product.getQuantity()) return requestedProduct.copy(count = ZERO)
        val promotionCycle = promotion.buy + promotion.get
        val reminder = requestedProduct.count % (promotionCycle)
        if (reminder < promotion.buy) return requestedProduct.copy(count = ZERO)
        val requestCount = promotionCycle - reminder
        return requestedProduct.copy(count = requestCount)
    }

    fun calculateNonPromotionalProducts(requestedProduct: RequestedProduct): RequestedProduct {
        val product = products.find { it.name == requestedProduct.name } ?: throw IllegalArgumentException()
        val promotion = promotions.find { it.name == product.promotion } ?: throw IllegalArgumentException()
        if (product.getQuantity() < requestedProduct.count) {
            val reminder = product.getQuantity().rem(promotion.buy + promotion.get)
            return requestedProduct.copy(count = requestedProduct.count + reminder - product.getQuantity())
        }
        return requestedProduct.copy(count = ZERO)
    }

    fun calculatePromotionalProducts(requestedProduct: RequestedProduct): RequestedProduct {
        val product = products.find { it.name == requestedProduct.name } ?: return requestedProduct
        val promotion = promotions.find { it.name == product.promotion } ?: return requestedProduct
        if (requestedProduct.count > product.getQuantity()) {
            val reminder = product.getQuantity().rem(promotion.buy + promotion.get)
            return requestedProduct.copy(count = product.getQuantity() - reminder)
        }
        val reminder = requestedProduct.count.rem(promotion.buy + promotion.get)
        return requestedProduct.copy(count = requestedProduct.count - reminder)
    }

    fun buyProduct(requestedProduct: RequestedProduct): PurchaseProduct {
        val product = products.find { it.name == requestedProduct.name } ?: throw IllegalArgumentException()
        val promotion = promotions.find { it.name == product.promotion }
        val applyCount = calculateApplyCount(requestedProduct)
        updateProductsQuantity(requestedProduct)
        return PurchaseProduct(
            product.name, requestedProduct.count, applyCount, product.price, getPromotionCount(applyCount, promotion)
        )
    }

    private fun getPromotionCount(applyCount: Int, promotion: Promotion?) =
        applyCount * ((promotion?.buy ?: ZERO) + (promotion?.get ?: ZERO))

    private fun calculateApplyCount(requestedProduct: RequestedProduct): Int {
        val product = products.find { it.name == requestedProduct.name } ?: throw IllegalArgumentException()
        val promotion = promotions.find { it.name == product.promotion } ?: return ZERO
        val currentDate = DateTimes.now().toLocalDate()
        if (isPromotion(requestedProduct, currentDate).not()) return ZERO
        if (requestedProduct.count > product.getQuantity()) {
            return product.getQuantity().div(promotion.buy + promotion.get)
        }
        return requestedProduct.count.div(promotion.buy + promotion.get)
    }

    private fun updateProductsQuantity(requestedProduct: RequestedProduct) {
        var currentRequestedProductCount = requestedProduct.count
        products.forEach { product ->
            if (product.name == requestedProduct.name && currentRequestedProductCount > ZERO) {
                currentRequestedProductCount = updateProductQuantity(product, currentRequestedProductCount)
            }
        }
    }

    private fun updateProductQuantity(product: Product, purchaseRemainingQuantity: Int): Int {
        val excess = product.calculateExcessQuantity(purchaseRemainingQuantity)
        product.deductQuantity(purchaseRemainingQuantity)
        return excess
    }

    companion object {
        private const val ZERO = 0
    }
}