package store

class Store(
    private val products: List<Product>
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