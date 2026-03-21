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
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class DataCollectionService(
    private val indicatorRepository: IndicatorRepository,
    private val indicatorDataRepository: IndicatorDataRepository,
    private val dataSourceClientFactory: DataSourceClientFactory,
    private val eventPublisher: ApplicationEventPublisher
) {

    private val log = LoggerFactory.getLogger(javaClass)

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

    fun collectBySource(source: DataSource) {
        val indicators = indicatorRepository.findBySource(source)
        collectIndicators(indicators, "source=$source")
    }

    fun collectBySourceAndCategories(source: DataSource, categories: List<IndicatorCategory>) {
        val indicators = indicatorRepository.findBySourceAndCategoryIn(source, categories)
        collectIndicators(indicators, "source=$source, categories=$categories")
    }

    private fun collectIndicators(indicators: List<Indicator>, context: String) {
        var successCount = 0
        var failCount = 0

        indicators.forEach { indicator ->
            if (collectLatestData(indicator)) successCount++ else failCount++
        }

        log.info("Collection completed ({}): {} success, {} failed out of {} indicators",
            context, successCount, failCount, indicators.size)
    }

    fun collectBySymbols(symbols: List<String>) {
        val indicators = symbols.mapNotNull { symbol ->
            indicatorRepository.findBySymbol(symbol) ?: run {
                log.warn("Indicator not found for symbol: {}", symbol)
                null
            }
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

        log.info("Collection completed: {} success, {} failed out of {} indicators",
            successCount, failCount, indicators.size)
    }

    @Transactional
    fun collectHistoricalData(indicator: Indicator, from: LocalDate, to: LocalDate): Int {
        return try {
            val client = dataSourceClientFactory.getClient(indicator.source)
            val dataPoints = client.fetchHistoricalData(indicator.symbol, from, to)

            if (dataPoints.isEmpty()) {
                log.warn("No historical data returned for {} ({}) from {} to {}",
                    indicator.name, indicator.symbol, from, to)
                return 0
            }

            dataPoints.forEach { dataPoint -> upsertDataPoint(indicator, dataPoint) }
            eventPublisher.publishEvent(IndicatorDataCollectedEvent(indicator.id))
            log.info("Collected {} historical data points for {} ({}) from {} to {}",
                dataPoints.size, indicator.name, indicator.symbol, from, to)
            dataPoints.size
        } catch (e: Exception) {
            log.error("Failed to collect historical data for {} ({}): {}",
                indicator.name, indicator.symbol, e.message)
            0
        }
    }

    fun collectHistoricalBySymbols(symbols: List<String>, from: LocalDate, to: LocalDate): Int {
        val indicators = symbols.mapNotNull { symbol ->
            indicatorRepository.findBySymbol(symbol) ?: run {
                log.warn("Indicator not found for symbol: {}", symbol)
                null
            }
        }

        var totalCount = 0
        indicators.forEach { indicator ->
            totalCount += collectHistoricalData(indicator, from, to)
        }

        log.info("Historical collection completed: {} total data points for {} indicators",
            totalCount, indicators.size)
        return totalCount
    }

    fun collectAllHistorical(from: LocalDate, to: LocalDate): Int {
        val indicators = indicatorRepository.findAll()
        var totalCount = 0
        indicators.forEach { indicator ->
            totalCount += collectHistoricalData(indicator, from, to)
        }
        log.info("Historical collection completed: {} total data points for all {} indicators",
            totalCount, indicators.size)
        return totalCount
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
