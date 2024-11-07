package store

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class StoreTest {
    private lateinit var defaultProducts: List<Product>
    private lateinit var defaultPromotion: List<Promotion>
    private lateinit var store: Store

    @BeforeEach
    fun setUp() {
        defaultProducts = listOf(
            Product("사과", 1000, 10, "MD추천상품"),
            Product("사과", 1000, 10, "null"),
            Product("수박", 2000, 10, "null"),
        )
        defaultPromotion = listOf(
            Promotion("MD추천상품", 3, 4, "2024-01-01", "2024-12-31"),
        )
        store = Store(defaultProducts, defaultPromotion)
    }

    @Test
    fun `각 상품의 재고 수량을 고려하여 결제 가능 여부 성공 테스트`() {
        val purchaseProduct = RequestedProduct("사과", 14)
        val result = store.hasProduct(purchaseProduct)
        val expected = true
        assertEquals(expected, result)
    }

    @Test
    fun `각 상품의 재고 수량을 고려하여 결제 가능 여부 싪패 테스트`() {
        val purchaseProduct = RequestedProduct("사과", 21)
        val result = store.hasProduct(purchaseProduct)
        val expected = false
        assertEquals(expected, result)
    }

    @Test
    fun `구매하는 제품이 프로모션인지 확인하는 기능 테스트`() {
        val purchaseProduct = RequestedProduct("수박", 1)
        val result = store.isPromotion(purchaseProduct)
        val expected = false
        assertEquals(expected, result)
    }

    @Test
    fun `구매할때 추가로 증정할 프로모션을 반환 하는 기능 테스트`() {
        val result = store.applyPromotionProduct(RequestedProduct("사과", 6))
        val expected = RequestedProduct("사과", 4)
        assertEquals(expected, result)
    }

    @ParameterizedTest
    @CsvSource(
        "10, 0",
        "13, 6",
        "0, 0",
        "1000,993"
    )
    fun `프로모션이 들어가지 않는 제품을 반환 하는 기능 테스트`(requestCount:Int,expectedCount:Int) {
        val result = store.getNonPromotionalProducts(RequestedProduct("사과", requestCount))
        val expected = RequestedProduct("사과", expectedCount)
        assertEquals(expected, result)
    }

    @Test
    fun `고객이 상품을 구매할 때마다, 결제된 수량만큼 해당 상품의 재고에서 차감하고 최신 상태를 반환하는 테스트`() {
        store.buyProduct(RequestedProduct("사과", 14))
        val result = listOf(
            Product("사과", 1000, 0, "MD추천 상품"),
            Product("사과", 1000, 6, "null"),
            Product("수박", 2000, 10, "null"),
        )
        val expected = store.getProducts()
        assertEquals(expected, result)
    }
}