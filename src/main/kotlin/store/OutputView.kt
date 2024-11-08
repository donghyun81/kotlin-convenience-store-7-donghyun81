package store

import java.text.DecimalFormat

class OutputView {

    fun printStart() {
        println("안녕하세요. W편의점입니다.")
    }

    fun printStoreProducts(products: List<Product>) {
        println("현재 보유하고 있는 상품입니다.\n")
        products.forEach { product ->
            println("- ${product.name} ${product.price}원 ${product.getQuantity()}개 ${product.promotion ?: ""}")
        }
        println()
    }

    fun printReceipt(purchaseProduct: List<Product>, promotionProducts: List<Product>) {
        printPurchaseProducts(purchaseProduct)
        printPromotionProducts(promotionProducts)
    }

    private fun printPurchaseProducts(purchaseProduct: List<Product>) {
        println("==============W 편의점================")
        println("상품명		수량	  금액")
        purchaseProduct.forEach { product ->
            println(
                "${product.name}\t\t${
                    product.getQuantity().toWonFormat()
                } \t${(product.price * product.getQuantity()).toWonFormat()}"
            )
        }
    }

    private fun printPromotionProducts(promotionProducts: List<Product>) {
        println("====== === === = 증    정 === === === === ===")
        promotionProducts.forEach { product ->
            println("${product.name}        ${product.getQuantity()}")
        }
    }

    private fun printTotalReceipt(
        totalPrice: Int,
        totalPriceCount: Int,
        promotionDiscount: Int,
        membershipDiscount: Int
    ) {
        println("====== === === === === === === === === === ===")
        println("총구매액        $totalPriceCount    ${totalPrice.toWonFormat()}")
        println("행사 할인 \t\t\t -$promotionDiscount")
        println("멤버십할인\t\t\t-$membershipDiscount")
    }

    private fun Int.toWonFormat(): String {
        val formatter = DecimalFormat("#,###")
        return formatter.format(this)
    }
}