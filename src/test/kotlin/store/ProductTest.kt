package store

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ProductTest {

    private lateinit var defaultProduct: Product

    @BeforeEach
    fun `setUp`() {
        defaultProduct = Product("사과", 1000, 10, "MD추천 상품")
    }

    @ParameterizedTest
    @CsvSource(
        "7, 17",
        "10, 20",
        "80, 90",
    )
    fun `제품의 수량을 증가시키는 기능 테스트`(deductCount: Int, expected: Int) {
        defaultProduct.increaseCount(deductCount)
        val result = defaultProduct.getQuantity()
        assertEquals(expected, result)
    }

    @ParameterizedTest
    @CsvSource(
        "4, 6",
        "10, 0",
        "0, 10",
    )
    fun `제품의 수량을 빼는 기능 테스트`(deductCount: Int, expected: Int) {
        defaultProduct.deductQuantity(deductCount)
        val result = defaultProduct.getQuantity()
        assertEquals(expected, result)
    }

    @ParameterizedTest
    @CsvSource(
        "1200, 0",
        "1000, 0",
        "99, 0",
    )
    fun `보유한 수량보다 많은 개수를 빼면 0을 반환하는지 테스트`(deductCount: Int, expected: Int) {
        defaultProduct.deductQuantity(deductCount)
        val result = defaultProduct.getQuantity()
        assertEquals(expected, result)
    }

    @ParameterizedTest
    @CsvSource(
        "12, 2",
        "8, 0",
        "18, 8",
    )
    fun `빼는 수량에서 현재 재고를 제외한 나머지 값을 반환하는지 테스트`(deductCount: Int, expected: Int) {
        val result = defaultProduct.calculateExcessQuantity(deductCount)
        assertEquals(expected, result)
    }
}