package com.econdashboard.service

import com.econdashboard.domain.Indicator
import com.econdashboard.enums.DataSource
import com.econdashboard.enums.IndicatorCategory
import com.econdashboard.exception.NotFoundException
import com.econdashboard.repository.IndicatorDataRepository
import com.econdashboard.repository.IndicatorRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("IndicatorService 단위 테스트")
class IndicatorServiceTest {

    private val indicatorRepository: IndicatorRepository = mockk()
    private val indicatorDataRepository: IndicatorDataRepository = mockk()
    private val indicatorCacheService: IndicatorCacheService = mockk()

    private lateinit var sut: IndicatorService

    @BeforeEach
    fun setup() {
        sut = IndicatorService(indicatorRepository, indicatorDataRepository, indicatorCacheService)
    }

    private fun createIndicator(
        id: Long = 1L,
        name: String = "S&P 500",
        symbol: String = "^GSPC",
        category: IndicatorCategory = IndicatorCategory.STOCK,
    ): Indicator {
        return Indicator(
            name = name,
            symbol = symbol,
            category = category,
            unit = "USD",
            source = DataSource.YAHOO,
            id = id
        )
    }

    @Nested
    @DisplayName("getAllIndicators")
    inner class GetAllIndicators {

        @Test
        @DisplayName("카테고리 없이 호출 시 전체 지표를 캐시에서 조회한다")
        fun shouldReturnAllIndicatorsFromCache() {
            val indicators = listOf(
                createIndicator(1L, "S&P 500", "^GSPC", IndicatorCategory.STOCK),
                createIndicator(2L, "Bitcoin", "bitcoin", IndicatorCategory.CRYPTO),
            )
            every { indicatorCacheService.findAllIndicators() } returns indicators

            val result = sut.getAllIndicators(null)

            assertEquals(2, result.size)
            assertEquals("S&P 500", result[0].name)
            assertEquals("Bitcoin", result[1].name)
            verify(exactly = 1) { indicatorCacheService.findAllIndicators() }
            verify(exactly = 0) { indicatorCacheService.findByCategory(any()) }
        }

        @Test
        @DisplayName("카테고리 지정 시 해당 카테고리만 캐시에서 조회한다")
        fun shouldReturnFilteredIndicatorsFromCache() {
            val stockIndicators = listOf(
                createIndicator(1L, "S&P 500", "^GSPC", IndicatorCategory.STOCK),
            )
            every { indicatorCacheService.findByCategory(IndicatorCategory.STOCK) } returns stockIndicators

            val result = sut.getAllIndicators(IndicatorCategory.STOCK)

            assertEquals(1, result.size)
            assertEquals(IndicatorCategory.STOCK, result[0].category)
            verify(exactly = 0) { indicatorCacheService.findAllIndicators() }
            verify(exactly = 1) { indicatorCacheService.findByCategory(IndicatorCategory.STOCK) }
        }

        @Test
        @DisplayName("지표가 없으면 빈 리스트를 반환한다")
        fun shouldReturnEmptyListWhenNoIndicators() {
            every { indicatorCacheService.findAllIndicators() } returns emptyList()

            val result = sut.getAllIndicators(null)

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    @DisplayName("getIndicatorById")
    inner class GetIndicatorById {

        @Test
        @DisplayName("존재하는 지표 ID로 조회 시 IndicatorResponse를 반환한다")
        fun shouldReturnIndicatorResponse() {
            val indicator = createIndicator(1L, "S&P 500", "^GSPC")
            every { indicatorRepository.findById(1L) } returns Optional.of(indicator)

            val result = sut.getIndicatorById(1L)

            assertEquals(1L, result.id)
            assertEquals("S&P 500", result.name)
            assertEquals("^GSPC", result.symbol)
        }

        @Test
        @DisplayName("존재하지 않는 지표 ID로 조회 시 NotFoundException을 던진다")
        fun shouldThrowNotFoundExceptionForInvalidId() {
            every { indicatorRepository.findById(999L) } returns Optional.empty()

            assertThrows(NotFoundException::class.java) {
                sut.getIndicatorById(999L)
            }
        }
    }

    @Nested
    @DisplayName("getCategories")
    inner class GetCategories {

        @Test
        @DisplayName("모든 IndicatorCategory enum 값을 반환한다")
        fun shouldReturnAllCategories() {
            val result = sut.getCategories()

            assertEquals(IndicatorCategory.entries.size, result.size)
            assertTrue(result.contains(IndicatorCategory.STOCK))
            assertTrue(result.contains(IndicatorCategory.CRYPTO))
            assertTrue(result.contains(IndicatorCategory.MACRO))
        }
    }
}
