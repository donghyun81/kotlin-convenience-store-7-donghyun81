package store

fun main() {
    val inputView = InputView()
    val outputView = OutputView()
    StoreController(inputView, outputView).run()
}
