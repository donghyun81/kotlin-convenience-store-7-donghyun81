package store.model

import store.common.ZERO

data class Product(
    val name: String,
    val price: Int,
    private var quantity: Int,
    val promotion: String?
) {
    fun deductQuantity(buyCount: Int) {
        quantity = (quantity - buyCount).coerceAtLeast(ZERO)
    }

    fun calculateExcessQuantity(buyCount: Int): Int {
        return (buyCount - quantity).coerceAtLeast(ZERO)
    }

    fun getQuantity() = quantity
}