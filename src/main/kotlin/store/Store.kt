package store

class Store(
    private val products: List<Product>,
) {

    fun getProducts() = products.toList()

    fun hasProduct(purchaseProduct: Product): Boolean {
        var currentPurchaseCount = purchaseProduct.getQuantity()
        products.map { product ->
            if (product.name == purchaseProduct.name) {
                currentPurchaseCount -= product.getQuantity()
            }
        }
        return currentPurchaseCount < 0
    }

    fun buyProduct(purchaseProduct: Product) {
        updateProductsQuantity(purchaseProduct)
    }

    private fun updateProductsQuantity(purchaseProduct: Product): Int {
        var purchaseRemainingQuantity = purchaseProduct.getQuantity()
        products.forEach { product ->
            if (product.name == purchaseProduct.name && purchaseRemainingQuantity > 0) {
                purchaseRemainingQuantity = applyPurchaseToProduct(product, purchaseRemainingQuantity)
            }
        }
        return purchaseRemainingQuantity
    }

    private fun applyPurchaseToProduct(product: Product, purchaseRemainingQuantity: Int): Int {
        val excess = product.calculateExcessQuantity(purchaseRemainingQuantity)
        product.deductQuantity(purchaseRemainingQuantity)
        return excess
    }
}