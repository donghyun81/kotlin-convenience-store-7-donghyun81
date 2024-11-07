package store

data class Product(
    val name: String,
    val price: Int,
    private var quantity: Int,
    val promotion: String?
) {
    fun increaseCount(presentationCount: Int) {
        quantity += presentationCount
    }

    fun deductQuantity(buyCount: Int) {
        quantity = (quantity - buyCount).coerceAtLeast(0)
    }

    fun calculateExcessQuantity(buyCount: Int): Int {
        return (buyCount - quantity).coerceAtLeast(0)
    }

    fun getQuantity() = quantity
}