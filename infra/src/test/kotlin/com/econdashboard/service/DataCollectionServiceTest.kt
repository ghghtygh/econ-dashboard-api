package com.econdashboard.service

import com.econdashboard.client.common.DataSourceClient
import com.econdashboard.client.common.DataSourceClientFactory
import com.econdashboard.client.common.ExternalDataPoint
import com.econdashboard.domain.Indicator
import com.econdashboard.domain.IndicatorData
import com.econdashboard.enums.DataSource
import com.econdashboard.enums.IndicatorCategory
import com.econdashboard.event.IndicatorDataCollectedEvent
import com.econdashboard.repository.IndicatorDataRepository
import com.econdashboard.repository.IndicatorRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import java.math.BigDecimal
import java.time.LocalDate

@DisplayName("DataCollectionService 단위 테스트")
class DataCollectionServiceTest {

    private val indicatorRepository: IndicatorRepository = mockk()
    private val indicatorDataRepository: IndicatorDataRepository = mockk()
    private val dataSourceClientFactory: DataSourceClientFactory = mockk()
    private val eventPublisher: ApplicationEventPublisher = mockk(relaxed = true)

    private lateinit var sut: DataCollectionService

    @BeforeEach
    fun setup() {
        sut = DataCollectionService(
            indicatorRepository, indicatorDataRepository, dataSourceClientFactory, eventPublisher
        )
    }

    private fun createIndicator(
        id: Long = 1L,
        symbol: String = "^GSPC",
        source: DataSource = DataSource.YAHOO,
    ): Indicator {
        return Indicator(
            name = "S&P 500",
            symbol = symbol,
            category = IndicatorCategory.STOCK,
            unit = "USD",
            source = source,
            id = id
        )
    }

    private fun createDataPoint(
        value: BigDecimal = BigDecimal("5000.00"),
        date: LocalDate = LocalDate.now(),
    ): ExternalDataPoint {
        return ExternalDataPoint(
            date = date,
            value = value,
            open = null,
            high = null,
            low = null,
            close = null,
            volume = null,
            change = null,
        )
    }

    @Nested
    @DisplayName("collectLatestData")
    inner class CollectLatestData {

        @Test
        @DisplayName("정상 수집 시 데이터를 저장하고 이벤트를 발행한다")
        fun shouldSaveDataAndPublishEvent() {
            val indicator = createIndicator()
            val dataPoint = createDataPoint()
            val client: DataSourceClient = mockk()

            every { dataSourceClientFactory.getClient(DataSource.YAHOO) } returns client
            every { client.fetchLatestPrice("^GSPC") } returns dataPoint
            every { indicatorDataRepository.findByIndicatorIdAndDate(1L, any()) } returns null
            every { indicatorDataRepository.save(any<IndicatorData>()) } returns mockk()

            val result = sut.collectLatestData(indicator)

            assertTrue(result)
            verify(exactly = 1) { indicatorDataRepository.save(any<IndicatorData>()) }
            verify(exactly = 1) { eventPublisher.publishEvent(match<IndicatorDataCollectedEvent> { it.indicatorId == 1L }) }
        }

        @Test
        @DisplayName("외부 API가 null 반환 시 false를 반환한다")
        fun shouldReturnFalseWhenNoDataReturned() {
            val indicator = createIndicator()
            val client: DataSourceClient = mockk()

            every { dataSourceClientFactory.getClient(DataSource.YAHOO) } returns client
            every { client.fetchLatestPrice("^GSPC") } returns null

            val result = sut.collectLatestData(indicator)

            assertFalse(result)
            verify(exactly = 0) { indicatorDataRepository.save(any<IndicatorData>()) }
            verify(exactly = 0) { eventPublisher.publishEvent(any()) }
        }

        @Test
        @DisplayName("외부 API 예외 발생 시 false를 반환한다")
        fun shouldReturnFalseOnException() {
            val indicator = createIndicator()
            val client: DataSourceClient = mockk()

            every { dataSourceClientFactory.getClient(DataSource.YAHOO) } returns client
            every { client.fetchLatestPrice("^GSPC") } throws RuntimeException("API timeout")

            val result = sut.collectLatestData(indicator)

            assertFalse(result)
            verify(exactly = 0) { indicatorDataRepository.save(any<IndicatorData>()) }
        }

        @Test
        @DisplayName("기존 데이터가 있으면 업데이트한다")
        fun shouldUpdateExistingData() {
            val indicator = createIndicator()
            val dataPoint = createDataPoint()
            val client: DataSourceClient = mockk()
            val existingData: IndicatorData = mockk(relaxed = true)

            every { dataSourceClientFactory.getClient(DataSource.YAHOO) } returns client
            every { client.fetchLatestPrice("^GSPC") } returns dataPoint
            every { indicatorDataRepository.findByIndicatorIdAndDate(1L, any()) } returns existingData
            every { indicatorDataRepository.save(existingData) } returns existingData

            val result = sut.collectLatestData(indicator)

            assertTrue(result)
            verify(exactly = 1) { indicatorDataRepository.save(existingData) }
            verify { existingData.value = BigDecimal("5000.00") }
        }
    }

    @Nested
    @DisplayName("collectBySymbols")
    inner class CollectBySymbols {

        @Test
        @DisplayName("여러 심볼을 병렬로 수집한다")
        fun shouldCollectMultipleSymbolsInParallel() {
            val indicator1 = createIndicator(1L, "^GSPC")
            val indicator2 = createIndicator(2L, "^IXIC")
            val dataPoint = createDataPoint()
            val client: DataSourceClient = mockk()

            every { indicatorRepository.findBySymbol("^GSPC") } returns indicator1
            every { indicatorRepository.findBySymbol("^IXIC") } returns indicator2
            every { dataSourceClientFactory.getClient(DataSource.YAHOO) } returns client
            every { client.fetchLatestPrice(any()) } returns dataPoint
            every { indicatorDataRepository.findByIndicatorIdAndDate(any(), any()) } returns null
            every { indicatorDataRepository.save(any<IndicatorData>()) } returns mockk()

            sut.collectBySymbols(listOf("^GSPC", "^IXIC"))

            verify(atLeast = 2) { indicatorDataRepository.save(any<IndicatorData>()) }
        }

        @Test
        @DisplayName("존재하지 않는 심볼은 건너뛴다")
        fun shouldSkipUnknownSymbols() {
            every { indicatorRepository.findBySymbol("UNKNOWN") } returns null

            sut.collectBySymbols(listOf("UNKNOWN"))

            verify(exactly = 0) { indicatorDataRepository.save(any<IndicatorData>()) }
        }
    }
}
