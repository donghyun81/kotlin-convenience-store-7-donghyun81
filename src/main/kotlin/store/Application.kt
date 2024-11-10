package store

import store.controller.StoreController
import store.view.InputView
import store.view.OutputView

fun main() {
    val inputView = InputView()
    val outputView = OutputView()
    StoreController(inputView, outputView).run()
}
