package store

data class Product(
    val name: String,
    val price: Int,
    private var quantity: Int,
    val promotion: String?
) {
    fun deductQuantity(buyCount: Int) {
        quantity = (quantity - buyCount).coerceAtLeast(0)
    }

    fun getQuantity() = quantity
}