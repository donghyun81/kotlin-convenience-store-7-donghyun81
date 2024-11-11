package store.common.enum

enum class OutputMessage(val message: String) {
    START_MESSAGE("안녕하세요. W편의점입니다."),
    CURRENT_PRODUCTS("현재 보유하고 있는 상품입니다.\n"),
    RECEIPT_MESSAGE_START("==============W 편의점================"),
    PURCHASE_ATTRIBUTES("상품명             수량     금액"),
    APPLY_MESSAGE_START("=============증 정==============="),
    TOTAL_START("===================================="),
}