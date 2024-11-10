package store.utils

fun <T> retryInput(inputAction: () -> T): T {
    while (true) {
        try {
            return inputAction()
        } catch (e: IllegalArgumentException) {
            println(e.message)
        }
    }
}

fun retryPurchase(buy: () -> Boolean) {
    while (true) {
        if (!buy()) return
    }
}