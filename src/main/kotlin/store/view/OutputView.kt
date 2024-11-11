package store.view

import store.common.enum.OutputMessage
import store.model.PurchaseProduct
import store.model.Product
import java.text.DecimalFormat

class OutputView {

    fun printStart() {
        println(OutputMessage.START_MESSAGE.message)
    }

    fun printStoreProducts(products: List<Product>) {
        println(OutputMessage.CURRENT_PRODUCTS.message)
        products.forEach { product ->
            printStoreProduct(product)
        }
        println()
    }

    private fun printStoreProduct(product: Product) {
        val name = product.name
        val price = product.price.toWonFormat() + WON
        val quantity = product.getQuantity().toCountFormat()
        val promotion = product.promotion ?: EMPTY
        println("$HYPHEN $name $price $quantity $promotion")
    }

    fun printReceipt(purchaseProducts: List<PurchaseProduct>, isMemberShip: Boolean) {
        printPurchaseProducts(purchaseProducts)
        printPromotionProducts(purchaseProducts)
        printPurchaseResult(purchaseProducts, isMemberShip)
    }

    private fun printPurchaseProducts(purchaseProduct: List<PurchaseProduct>) {
        println(OutputMessage.RECEIPT_MESSAGE_START.message)
        println(OutputMessage.PURCHASE_ATTRIBUTES.message)
        purchaseProduct.forEach { product ->
            val name = product.name.receiptFormat(RECEIPT_START_BLANK_COUNT)
            val count = product.count.toString().receiptFormat(QUANTITY_BLANK_COUNT)
            val price = product.count * product.price
            println("$name${count}${price.toWonFormat()}")
        }
    }

    private fun printPromotionProducts(purchaseProducts: List<PurchaseProduct>) {
        println(OutputMessage.APPLY_MESSAGE_START.message)
        purchaseProducts.forEach { product ->
            if (product.apply > 0) println("${product.name.receiptFormat(RECEIPT_START_BLANK_COUNT)}${product.apply}")
        }
    }

    private fun printPurchaseResult(purchaseProducts: List<PurchaseProduct>, isMemberShip: Boolean) {
        val totalPrice = purchaseProducts.sumOf { it.count * it.price }
        val promotionDiscount = purchaseProducts.sumOf { it.apply * it.price }
        val membershipDiscount = getMembershipDiscount(isMemberShip, totalPrice, purchaseProducts)
        println(OutputMessage.TOTAL_START.message)
        printTotalAmount(purchaseProducts, totalPrice)
        printDiscountResult(promotionDiscount, membershipDiscount)
        printPayment(totalPrice - promotionDiscount - membershipDiscount)
    }

    private fun printTotalAmount(purchaseProducts: List<PurchaseProduct>, totalPrice: Int) {
        val purchaseTotalProductCount =
            purchaseProducts.sumOf { it.count }.toString().receiptFormat(QUANTITY_BLANK_COUNT)
        println("${TOTAL_PURCHASE_AMOUNT.receiptFormat(RECEIPT_START_BLANK_COUNT)}$purchaseTotalProductCount${totalPrice.toWonFormat()}")
    }

    private fun printDiscountResult(promotionDiscount: Int, membershipDiscount: Int) {
        println("${TOTAL_PROMOTION_DISCOUNT.receiptFormat(DISCOUNT_BLANK_COUNT)}$MINUS${promotionDiscount.toWonFormat()}")
        println("${MEMBERSHIP_DISCOUNT.receiptFormat(DISCOUNT_BLANK_COUNT)}$MINUS${membershipDiscount.toWonFormat()}")
    }

    private fun printPayment(payment: Int) {
        println("${TOTAL_PAYMENT.receiptFormat(PAYMENT_BLANK_COUNT)}${payment.toWonFormat()}")
    }

    private fun getMembershipDiscount(
        isMemberShip: Boolean,
        totalPrice: Int,
        purchaseProducts: List<PurchaseProduct>
    ): Int {
        val promotionPrice = purchaseProducts.map { it.promotionCount * it.price }.sumOf { it }
        if (isMemberShip) return (totalPrice - promotionPrice).times(MEMBERSHIP_PERCENT).toInt()
            .coerceAtMost(MAX_MEMBERSHIP_AMOUNT)
        return ZERO
    }

    private fun String.receiptFormat(blankCount: Int) = this + BLANK.repeat(blankCount - this.length)

    private fun Int.toWonFormat(): String {
        val formatter = DecimalFormat(WON_FORM)
        return formatter.format(this)
    }

    private fun Int.toCountFormat(): String {
        if (this == ZERO) return NON_QUANTITY
        return toString() + COUNT
    }

    companion object {
        private const val MEMBERSHIP_PERCENT = 0.3
        private const val MAX_MEMBERSHIP_AMOUNT = 8000
        private const val NON_QUANTITY = "재고 없음"
        private const val COUNT = "개"
        private const val WON = "원"
        private const val EMPTY = ""
        private const val BLANK = " "
        private const val WON_FORM = "#,###"
        private const val HYPHEN = "-"
        private const val MINUS = "-"
        private const val ZERO = 0
        private const val TOTAL_PURCHASE_AMOUNT = "총구매액"
        private const val TOTAL_PROMOTION_DISCOUNT = "행사 할인"
        private const val MEMBERSHIP_DISCOUNT = "멤버십할인"
        private const val TOTAL_PAYMENT = "내실돈"
        private const val DISCOUNT_BLANK_COUNT = 22
        private const val PAYMENT_BLANK_COUNT = 23
        private const val RECEIPT_START_BLANK_COUNT = 16
        private const val QUANTITY_BLANK_COUNT = 7
    }
}