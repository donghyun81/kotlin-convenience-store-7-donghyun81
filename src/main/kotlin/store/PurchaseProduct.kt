package store

data class PurchaseProduct(
    val name: String,
    val count: Int,
    val apply: Int,
    val price: Int,
    val promotionCount: Int = 0
)