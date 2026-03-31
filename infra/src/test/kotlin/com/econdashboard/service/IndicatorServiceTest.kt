package com.econdashboard.service

import com.econdashboard.domain.Indicator
import com.econdashboard.domain.IndicatorData
import com.econdashboard.dto.IndicatorDataResponse
import com.econdashboard.dto.IndicatorSeriesRequest
import com.econdashboard.enums.DataSource
import com.econdashboard.enums.IndicatorCategory
import com.econdashboard.exception.NotFoundException
import com.econdashboard.repository.IndicatorDataRepository
import com.econdashboard.repository.IndicatorRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class IndicatorServiceTest {

    private val indicatorRepository: IndicatorRepository = mockk()
    private val indicatorDataRepository: IndicatorDataRepository = mockk()
    private val indicatorCacheService: IndicatorCacheService = mockk()

    private lateinit var indicatorService: IndicatorService

    private lateinit var sampleIndicator: Indicator

    @BeforeEach
    fun setUp() {
        indicatorService = IndicatorService(indicatorRepository, indicatorDataRepository, indicatorCacheService)
        sampleIndicator = Indicator(
            name = "S&P 500",
            symbol = "SPX",
            category = IndicatorCategory.STOCK,
            unit = "포인트",
            source = DataSource.YAHOO,
            description = "미국 대표 주가지수"
        ).apply {
            // id는 JPA에서 자동 생성이지만 테스트용으로 reflection을 사용
            val idField = Indicator::class.java.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, 1L)
        }
    }

    @Test
    fun `getAllIndicators - 카테고리 없이 전체 조회`() {
        every { indicatorCacheService.findAllIndicators() } returns listOf(sampleIndicator)

        val result = indicatorService.getAllIndicators(null)

        assertEquals(1, result.size)
        assertEquals("S&P 500", result[0].name)
        assertEquals("SPX", result[0].symbol)
        verify { indicatorCacheService.findAllIndicators() }
    }

    @Test
    fun `getAllIndicators - 카테고리 필터`() {
        every { indicatorCacheService.findByCategory(IndicatorCategory.STOCK) } returns listOf(sampleIndicator)

        val result = indicatorService.getAllIndicators(IndicatorCategory.STOCK)

        assertEquals(1, result.size)
        verify { indicatorCacheService.findByCategory(IndicatorCategory.STOCK) }
    }

    @Test
    fun `getIndicatorById - 정상 조회`() {
        every { indicatorRepository.findById(1L) } returns Optional.of(sampleIndicator)

        val result = indicatorService.getIndicatorById(1L)

        assertEquals(1L, result.id)
        assertEquals("S&P 500", result.name)
    }

    @Test
    fun `getIndicatorById - 존재하지 않는 지표`() {
        every { indicatorRepository.findById(999L) } returns Optional.empty()

        assertThrows<NotFoundException> {
            indicatorService.getIndicatorById(999L)
        }
    }

    @Test
    fun `getCategories - 전체 카테고리 반환`() {
        val result = indicatorService.getCategories()

        assertEquals(IndicatorCategory.entries.size, result.size)
    }

    @Test
    fun `getIndicatorData - 날짜 범위로 시계열 데이터 조회`() {
        val pageable = PageRequest.of(0, 100)
        val indicatorData = IndicatorData(
            indicator = sampleIndicator,
            date = LocalDate.of(2025, 1, 15),
            value = BigDecimal("5000.00")
        ).apply {
            val idField = IndicatorData::class.java.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, 1L)
        }
        val page = PageImpl(listOf(indicatorData), pageable, 1)

        every { indicatorRepository.findById(1L) } returns Optional.of(sampleIndicator)
        every {
            indicatorDataRepository.findByIndicatorIdAndDateBetweenOrderByDateAsc(
                1L, any(), any(), pageable
            )
        } returns page

        val result = indicatorService.getIndicatorData(
            1L, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), pageable
        )

        assertEquals(1, result.totalElements)
        assertEquals(BigDecimal("5000.00"), result.content[0].value)
    }

    @Test
    fun `getIndicatorData - 날짜 미지정시 기본값 적용`() {
        val pageable = PageRequest.of(0, 100)
        val page = PageImpl<IndicatorData>(emptyList(), pageable, 0)

        every { indicatorRepository.findById(1L) } returns Optional.of(sampleIndicator)
        every {
            indicatorDataRepository.findByIndicatorIdAndDateBetweenOrderByDateAsc(
                1L, any(), any(), pageable
            )
        } returns page

        val result = indicatorService.getIndicatorData(1L, null, null, pageable)

        assertNotNull(result)
    }

    @Test
    fun `getMultipleIndicatorSeries - 복수 지표 시계열 조회`() {
        val indicatorData = IndicatorData(
            indicator = sampleIndicator,
            date = LocalDate.of(2025, 6, 1),
            value = BigDecimal("5200.00")
        ).apply {
            val idField = IndicatorData::class.java.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, 1L)
        }

        every { indicatorRepository.findById(1L) } returns Optional.of(sampleIndicator)
        every {
            indicatorCacheService.findSeriesData(1L, any(), any())
        } returns listOf(indicatorData)

        val request = IndicatorSeriesRequest(
            indicatorIds = listOf(1L),
            startDate = LocalDate.of(2025, 1, 1),
            endDate = LocalDate.of(2025, 12, 31)
        )

        val result = indicatorService.getMultipleIndicatorSeries(request)

        assertEquals(1, result.size)
        assertEquals(1L, result[0].indicatorId)
        assertEquals(1, result[0].data.size)
    }
}
