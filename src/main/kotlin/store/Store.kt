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

    fun buyProduct(requestedProduct: RequestedProduct) {
        updateProductsQuantity(requestedProduct)
    }

    private fun updateProductsQuantity(requestedProduct: RequestedProduct): Int {
        var currentRequestedProductCount = requestedProduct.count
        products.forEach { product ->
            if (product.name == requestedProduct.name && currentRequestedProductCount > 0) {
                currentRequestedProductCount = applyPurchaseToProduct(product, currentRequestedProductCount)
            }
        }
        return currentRequestedProductCount
    }

    private fun applyPurchaseToProduct(product: Product, purchaseRemainingQuantity: Int): Int {
        val excess = product.calculateExcessQuantity(purchaseRemainingQuantity)
        product.deductQuantity(purchaseRemainingQuantity)
        return excess
    }
}