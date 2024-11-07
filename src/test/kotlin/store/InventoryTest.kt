package store

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InventoryTest {
    private lateinit var defaultProducts: MutableList<Product>

    @BeforeEach
    fun `setUp`() {
        defaultProducts = mutableListOf(
            Product("사과", 1000, 10, "MD추천 상품"),
            Product("사과", 1000, 10, "null"),
            Product("수박", 1000, 10, "MD추천 상품"),
        )
    }

    @Test
    fun `각 상품의 재고 수량을 고려하여 결제 가능 여부 성공 테스트`() {
        val inventory = Inventory(defaultProducts)
        val purchaseProduct = Product("사과", 1000, 14, "MD추천 상품")
        val result = inventory.hasProduct(purchaseProduct)
        val expected = true
        assertEquals(expected, result)
    }

    @Test
    fun `각 상품의 재고 수량을 고려하여 결제 가능 여부 싪패 테스트`() {
        val products = mutableListOf(Product("사과", 1000, 10, "MD추천 상품"))
        val inventory = Inventory(products)
        val purchaseProduct = Product("사과", 1000, 11, "MD추천 상품")
        val result = inventory.hasProduct(purchaseProduct)
        val expected = false
        assertEquals(expected, result)
    }


    @Test
    fun `고객이 상품을 구매할 때마다, 결제된 수량만큼 해당 상품의 재고에서 차감하고 최신 상태를 반환하는 테스트`() {
        val inventory = Inventory(defaultProducts)
        val purchaseProduct = Product("사과", 1000, 14, "MD추천 상품")

        inventory.buyProduct(purchaseProduct)

        val result = listOf(
            Product("사과", 1000, 0, "MD추천 상품"),
            Product("사과", 1000, 6, "null"),
            Product("수박", 1000, 10, "MD추천 상품")
        )

        val expected = inventory.getProducts()
        assertEquals(expected, result)
    }
}