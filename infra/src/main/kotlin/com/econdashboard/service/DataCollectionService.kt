package com.econdashboard.service

import com.econdashboard.client.common.DataSourceClientFactory
import com.econdashboard.client.common.ExternalDataPoint
import com.econdashboard.domain.Indicator
import com.econdashboard.domain.IndicatorData
import com.econdashboard.enums.DataSource
import com.econdashboard.enums.IndicatorCategory
import com.econdashboard.event.IndicatorDataCollectedEvent
import com.econdashboard.repository.IndicatorDataRepository
import com.econdashboard.repository.IndicatorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DataCollectionService(
    private val indicatorRepository: IndicatorRepository,
    private val indicatorDataRepository: IndicatorDataRepository,
    private val dataSourceClientFactory: DataSourceClientFactory,
    private val eventPublisher: ApplicationEventPublisher
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val MAX_CONCURRENCY = 3
        private const val COLLECTION_TIMEOUT_MS = 4L * 60 * 1000 // 4분 타임아웃
    }

    @Transactional
    fun collectLatestData(indicator: Indicator): Boolean {
        return try {
            val client = dataSourceClientFactory.getClient(indicator.source)
            val dataPoint = client.fetchLatestPrice(indicator.symbol) ?: run {
                log.warn("No data returned for {} ({})", indicator.name, indicator.symbol)
                return false
            }

            upsertDataPoint(indicator, dataPoint)
            eventPublisher.publishEvent(IndicatorDataCollectedEvent(indicator.id))
            log.info("Collected latest data for {} ({}): {} on {}",
                indicator.name, indicator.symbol, dataPoint.value, dataPoint.date)
            true
        } catch (e: Exception) {
            log.error("Failed to collect data for {} ({}): {}",
                indicator.name, indicator.symbol, e.message)
            false
        }
    }

    fun collectBySourceAndCategories(source: DataSource, categories: List<IndicatorCategory>) {
        val indicators = indicatorRepository.findBySourceAndCategoryIn(source, categories)
        if (indicators.isEmpty()) {
            log.warn("No indicators found for source={}, categories={}", source, categories)
            return
        }

        var successCount = 0
        var failCount = 0

        indicators.forEach { indicator ->
            if (collectLatestData(indicator)) {
                successCount++
            } else {
                failCount++
            }
        }

        log.info("Collection by source={} completed: {} success, {} failed out of {} indicators",
            source, successCount, failCount, indicators.size)
    }

    fun collectBySymbols(symbols: List<String>) {
        val indicators = symbols.mapNotNull { symbol ->
            indicatorRepository.findBySymbol(symbol) ?: run {
                log.warn("Indicator not found for symbol: {}", symbol)
                null
            }
        }

        if (indicators.isEmpty()) return

        val semaphore = Semaphore(MAX_CONCURRENCY)

        val results = runBlocking {
            withTimeout(COLLECTION_TIMEOUT_MS) {
                indicators.map { indicator ->
                    async(Dispatchers.IO) {
                        semaphore.withPermit {
                            collectLatestData(indicator)
                        }
                    }
                }.awaitAll()
            }
        }

        val successCount = results.count { it }
        val failCount = results.count { !it }

        log.info("Parallel collection completed: {} success, {} failed out of {} indicators",
            successCount, failCount, indicators.size)
    }

    private fun upsertDataPoint(indicator: Indicator, dataPoint: ExternalDataPoint) {
        val existing = indicatorDataRepository.findByIndicatorIdAndDate(indicator.id, dataPoint.date)

        if (existing != null) {
            existing.value = dataPoint.value
            existing.open = dataPoint.open
            existing.high = dataPoint.high
            existing.low = dataPoint.low
            existing.close = dataPoint.close
            existing.volume = dataPoint.volume
            existing.change = dataPoint.change
            indicatorDataRepository.save(existing)
            log.debug("Updated existing data for {} on {}", indicator.symbol, dataPoint.date)
        } else {
            val indicatorData = IndicatorData(
                indicator = indicator,
                date = dataPoint.date,
                value = dataPoint.value,
                open = dataPoint.open,
                high = dataPoint.high,
                low = dataPoint.low,
                close = dataPoint.close,
                volume = dataPoint.volume,
                change = dataPoint.change
            )
            indicatorDataRepository.save(indicatorData)
            log.debug("Inserted new data for {} on {}", indicator.symbol, dataPoint.date)
        }
    }
}
