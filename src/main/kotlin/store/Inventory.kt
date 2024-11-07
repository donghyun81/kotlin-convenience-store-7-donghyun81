package store

class Inventory(
    private val products: MutableList<Product>
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
}