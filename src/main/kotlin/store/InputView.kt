package store

import camp.nextstep.edu.missionutils.Console

class InputView {
    fun readBuyItem(): String {
        println("구매하실 상품명과 수량을 입력해 주세요. (예: [사이다-2],[감자칩-1])")
        return Console.readLine()
    }

    fun readHasNotPromotionProduct(requestedProduct: RequestedProduct): String {
        println("현재 ${requestedProduct.name} ${requestedProduct.count}개는 프로모션 할인이 적용되지 않습니다. 그래도 구매하시겠습니까? (Y/N)")
        return Console.readLine()
    }

    fun readAddPromotionProduct(requestedProduct: RequestedProduct): String {
        println("현재 ${requestedProduct.name}은(는) ${requestedProduct.count}개를 무료로 더 받을 수 있습니다. 추가하시겠습니까? (Y/N)\n")
        return Console.readLine()
    }

    fun readIsMembershipDiscount(): String {
        println("멤버십 할인을 받으시겠습니까? (Y/N)")
        return Console.readLine()
    }

    fun readRetryBuy(): String {
        println("감사합니다. 구매하고 싶은 다른 상품이 있나요? (Y/N)")
        return Console.readLine()
    }
}