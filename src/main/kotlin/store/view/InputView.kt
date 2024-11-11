package store.view

import camp.nextstep.edu.missionutils.Console
import store.common.enum.InputMessage
import store.model.RequestedProduct

class InputView {
    fun readPurchaseInput(): String {
        println(InputMessage.PURCHASE_PRODUCTS.message)
        return Console.readLine()
    }

    fun confirmNonPromotionalPurchase(requestedProduct: RequestedProduct): String {
        val message = NON_PROMOTIONAL_PURCHASE_MESSAGE_FORM.format(requestedProduct.name, requestedProduct.count)
        println(message)
        return Console.readLine()
    }

    fun confirmPromotionAddition(requestedProduct: RequestedProduct): String {
        val message = PROMOTION_ADDITION.format(requestedProduct.name, requestedProduct.count)
        println(message)
        return Console.readLine()
    }

    fun confirmMembershipDiscount(): String {
        println(InputMessage.MEMBERSHIP_DISCOUNT.message)
        return Console.readLine()
    }

    fun confirmAdditionalPurchase(): String {
        println(InputMessage.ADDITIONAL_PURCHASE.message)
        return Console.readLine()
    }

    companion object {
        private const val NON_PROMOTIONAL_PURCHASE_MESSAGE_FORM = "현재 %s %d개는 프로모션 할인이 적용되지 않습니다. 그래도 구매하시겠습니까? (Y/N)"
        private const val PROMOTION_ADDITION = "현재 %s은(는) %d개를 무료로 더 받을 수 있습니다. 추가하시겠습니까? (Y/N)"
    }
}