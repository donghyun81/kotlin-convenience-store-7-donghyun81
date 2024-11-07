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
        return currentRequestedProductCount < 0
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
        val reminder = requestedProduct.count.rem(promotion.buy)
        return requestedProduct.copy(count = promotion.get - reminder)
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

    fun buyProduct(requestedProduct: RequestedProduct) {
        updateProductsQuantity(requestedProduct)
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