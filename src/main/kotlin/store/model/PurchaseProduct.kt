package store.model

import store.common.ZERO

data class PurchaseProduct(
    val name: String,
    val count: Int,
    val apply: Int,
    val price: Int,
    val promotionCount: Int = ZERO
)