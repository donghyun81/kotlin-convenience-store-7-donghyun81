package store

import java.text.DecimalFormat

class OutputView {

    fun printStart() {
        println("안녕하세요. W편의점입니다.")
    }

    fun printStoreProducts(products: List<Product>) {
        println("현재 보유하고 있는 상품입니다.\n")
        products.forEach { product ->
            println(
                "- ${product.name} ${product.price.toWonFormat()}원 ${
                    product.getQuantity().toCountFormat()
                } ${product.promotion ?: ""}"
            )
        }
        println()
    }

    private fun Int.toCountFormat(): String {
        if (this == 0) return "재고 없음"
        return toString() + "개"
    }

    fun printReceipt(purchaseProducts: List<PurchaseProduct>) {
        printPurchaseProducts(purchaseProducts)
        printPromotionProducts(purchaseProducts)
        printTotalReceipt(purchaseProducts)
    }

    private fun printPurchaseProducts(purchaseProduct: List<PurchaseProduct>) {
        println("==============W 편의점================")
        println("상품명		수량	  금액")
        purchaseProduct.forEach { product ->
            println(
                "${product.name}\t\t${
                    product.count
                } \t${(product.count * product.price).toWonFormat()}"
            )
        }
    }

    private fun printPromotionProducts(purchaseProducts: List<PurchaseProduct>) {
        println("====== === === = 증    정 === === === === ===")
        purchaseProducts.forEach { product ->
            println("${product.name}        ${product.apply}")
        }
    }

    private fun printTotalReceipt(
        purchaseProducts: List<PurchaseProduct>
    ) {
        val totalPrice = purchaseProducts.sumOf { it.count * it.price }
        val promotionDiscount = purchaseProducts.sumOf { it.apply * it.price }
        val promotionPrice = purchaseProducts.map { it.promotionCount * it.price }.sumOf { it }
        println(promotionPrice)
        val membershipDiscount = (totalPrice - promotionPrice).times(0.3).toInt()
        println("====== === === === === === === === === === ===")
        println("총구매액        ${purchaseProducts.sumOf { it.count }}    ${totalPrice.toWonFormat()}")
        println("행사 할인 \t\t\t -$promotionDiscount")
        println("멤버십할인\t\t\t-$membershipDiscount")
        println("내실돈\t\t\t ${totalPrice - promotionDiscount - membershipDiscount}")
    }

    private fun Int.toWonFormat(): String {
        val formatter = DecimalFormat("#,###")
        return formatter.format(this)
    }
}