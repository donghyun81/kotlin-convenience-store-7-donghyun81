package store

import camp.nextstep.edu.missionutils.DateTimes
import java.time.LocalDate

class Store(
    private val products: List<Product>,
    private val promotions: List<Promotion>
) {
    fun getProducts() = products.toList()

    fun hasProduct(requestedProduct: RequestedProduct): Boolean {
        var remainingRequestCount = requestedProduct.count
        products.map { product ->
            if (product.name == requestedProduct.name) {
                remainingRequestCount -= product.getQuantity()
            }
        }
        return remainingRequestCount <= 0
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
        if (requestedProduct.count >= product.getQuantity()) return requestedProduct.copy(count = 0)
        val promotionCycle = promotion.buy + promotion.get
        val reminder = requestedProduct.count % (promotionCycle)
        if (reminder < promotion.buy) return requestedProduct.copy(count = 0)
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
        return requestedProduct.copy(count = 0)
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
            product.name,
            count = requestedProduct.count,
            apply = applyCount,
            price = product.price,
            promotionCount = applyCount * ((promotion?.buy ?: 0) + (promotion?.get ?: 0))
        )
    }

    private fun calculateApplyCount(requestedProduct: RequestedProduct): Int {
        val product = products.find { it.name == requestedProduct.name } ?: throw IllegalArgumentException()
        val promotion = promotions.find { it.name == product.promotion } ?: return 0
        val currentDate = DateTimes.now().toLocalDate()
        if (isPromotion(requestedProduct, currentDate).not()) return 0
        if (requestedProduct.count > product.getQuantity()) {
            return product.getQuantity().div(promotion.buy + promotion.get)
        }
        return requestedProduct.count.div(promotion.buy + promotion.get)
    }

    private fun updateProductsQuantity(requestedProduct: RequestedProduct) {
        var currentRequestedProductCount = requestedProduct.count
        products.forEach { product ->
            if (product.name == requestedProduct.name && currentRequestedProductCount > 0) {
                currentRequestedProductCount = updateProductQuantity(product, currentRequestedProductCount)
            }
        }
    }

    private fun updateProductQuantity(product: Product, purchaseRemainingQuantity: Int): Int {
        val excess = product.calculateExcessQuantity(purchaseRemainingQuantity)
        product.deductQuantity(purchaseRemainingQuantity)
        return excess
    }
}