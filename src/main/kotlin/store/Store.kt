package store

class Store(
    private val products: List<Product>,
    private val promotions: List<Promotion>
) {

    fun getProducts() = products.toList()

    fun hasProduct(requestedProduct: RequestedProduct): Boolean {
        var currentRequestedProductCount = requestedProduct.count
        products.map { product ->
            if (product.name == requestedProduct.name) {
                currentRequestedProductCount -= product.getQuantity()
            }
        }
        return currentRequestedProductCount <= 0
    }

    fun isPromotion(requestedProduct: RequestedProduct): Boolean {
        val product = products.find { it.name == requestedProduct.name } ?: return false
        promotions.find { it.name == product.promotion } ?: return false
        return true
    }

    fun applyPromotionProduct(requestedProduct: RequestedProduct): RequestedProduct {
        val product = products.find { it.name == requestedProduct.name } ?: throw IllegalArgumentException()
        val promotion = promotions.find { it.name == product.promotion } ?: throw IllegalArgumentException()
        if (product.getQuantity() < requestedProduct.count) return requestedProduct.copy(count = 0)
        val reminder = requestedProduct.count.rem(promotion.buy + promotion.get)
        val requestCount = if (reminder == promotion.buy) promotion.get else 0
        return requestedProduct.copy(count = requestCount)
    }

    fun getNonPromotionalProducts(requestedProduct: RequestedProduct): RequestedProduct {
        val product = products.find { it.name == requestedProduct.name } ?: throw IllegalArgumentException()
        val promotion = promotions.find { it.name == product.promotion } ?: throw IllegalArgumentException()
        if (product.getQuantity() < requestedProduct.count) {
            val reminder = product.getQuantity().rem(promotion.buy + promotion.get)
            return requestedProduct.copy(count = requestedProduct.count + reminder - product.getQuantity())
        }
        return requestedProduct.copy(count = 0)
    }

    fun getPromotionalProducts(requestedProduct: RequestedProduct): RequestedProduct {
        val product = products.find { it.name == requestedProduct.name } ?: return requestedProduct
        val promotion = promotions.find { it.name == product.promotion } ?: return requestedProduct
        if (requestedProduct.count > product.getQuantity()) {
            val reminder = product.getQuantity().rem(promotion.buy + promotion.get)
            return requestedProduct.copy(count = product.getQuantity() - reminder)
        }
        val reminder = requestedProduct.count.rem(promotion.buy + promotion.get)
        return requestedProduct.copy(count = requestedProduct.count - reminder)
    }

    private fun getApplyCount(requestedProduct: RequestedProduct): Int {
        val product = products.find { it.name == requestedProduct.name } ?: throw IllegalArgumentException()
        val promotion = promotions.find { it.name == product.promotion } ?: return 0
        if (requestedProduct.count > product.getQuantity()) {
            return product.getQuantity().div(promotion.buy + promotion.get)
        }
        return requestedProduct.count.div(promotion.buy + promotion.get)
    }

    fun buyProduct(requestedProduct: RequestedProduct): PurchaseProduct {
        val product = products.find { it.name == requestedProduct.name } ?: throw IllegalArgumentException()
        val promotion = promotions.find { it.name == product.promotion }
        val applyCount = getApplyCount(requestedProduct)
        updateProductsQuantity(requestedProduct)
        return PurchaseProduct(
            product.name,
            count = requestedProduct.count,
            apply = applyCount,
            price = product.price,
            promotionCount = applyCount * ((promotion?.buy ?: 0) + (promotion?.get ?: 0))
        )
    }

    private fun updateProductsQuantity(requestedProduct: RequestedProduct) {
        var currentRequestedProductCount = requestedProduct.count
        products.forEach { product ->
            if (product.name == requestedProduct.name && currentRequestedProductCount > 0) {
                currentRequestedProductCount = applyPurchaseToProduct(product, currentRequestedProductCount)
            }
        }
    }

    private fun applyPurchaseToProduct(product: Product, purchaseRemainingQuantity: Int): Int {
        val excess = product.calculateExcessQuantity(purchaseRemainingQuantity)
        product.deductQuantity(purchaseRemainingQuantity)
        return excess
    }
}