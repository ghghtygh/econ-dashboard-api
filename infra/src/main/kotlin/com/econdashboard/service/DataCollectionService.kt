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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

@Service
class DataCollectionService(
    private val indicatorRepository: IndicatorRepository,
    private val indicatorDataRepository: IndicatorDataRepository,
    private val dataSourceClientFactory: DataSourceClientFactory,
    private val eventPublisher: ApplicationEventPublisher
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 외부 API rate limit을 고려한 동시성 제한.
     * Yahoo Finance: ~2000/hour, CoinGecko free: ~30/min
     * 안전하게 동시 3개로 제한
     */
    private val concurrencyLimit = Semaphore(3)
    private val executor = Executors.newFixedThreadPool(3)

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

        val results = collectInParallel(indicators)
        log.info("Collection by source={} completed: {} success, {} failed out of {} indicators",
            source, results.first, results.second, indicators.size)
    }

    fun collectBySymbols(symbols: List<String>) {
        val indicators = symbols.mapNotNull { symbol ->
            indicatorRepository.findBySymbol(symbol) ?: run {
                log.warn("Indicator not found for symbol: {}", symbol)
                null
            }
        }

        val results = collectInParallel(indicators)
        log.info("Collection completed: {} success, {} failed out of {} indicators",
            results.first, results.second, indicators.size)
    }


    /**
     * 특정 지표의 이력 데이터를 backfill합니다.
     * 이미 존재하는 날짜는 upsert로 갱신합니다.
     *
     * @return 저장된 데이터 포인트 수
     */
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
            dataPoints.forEach { upsertDataPoint(indicator, it) }
            log.info("Backfilled {} data points for {} ({}) from {} to {}",
                dataPoints.size, indicator.name, indicator.symbol, from, to)
            dataPoints.size
        } catch (e: Exception) {
            log.error("Historical backfill failed for {} ({}): {}",
                indicator.name, indicator.symbol, e.message)
            0
        }
    }

    /**
     * 모든 지표에 대해 이력 데이터를 backfill합니다.
     * 데이터가 minDataPoints 미만인 지표만 대상으로 합니다.
     *
     * @param from backfill 시작일
     * @param to   backfill 종료일
     * @param minDataPoints 이 값 이상 데이터가 있으면 스킵
     */
    fun backfillAllIndicators(from: LocalDate, to: LocalDate, minDataPoints: Int = 20) {
        val indicators = indicatorRepository.findAll()
        log.info("Starting historical backfill for {} indicators (from={}, to={})", indicators.size, from, to)

        var skipped = 0
        var total = 0

        indicators.forEach { indicator ->
            val existingData = indicatorDataRepository.findByIndicatorIdAndDateBetweenOrderByDateAsc(
                indicator.id, from, to
            )
            if (existingData.size >= minDataPoints) {
                log.debug("Skipping backfill for {} ({}): {} data points already exist",
                    indicator.name, indicator.symbol, existingData.size)
                skipped++
                return@forEach
            }

            try {
                concurrencyLimit.acquire()
                try {
                    val count = collectHistoricalData(indicator, from, to)
                    total += count
                } finally {
                    concurrencyLimit.release()
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                log.error("Backfill interrupted for {}", indicator.symbol)
            }
        }

        log.info("Historical backfill completed: {} indicators skipped, {} total data points saved",
            skipped, total)
    }

    /**
     * 복수 지표를 병렬로 수집합니다.
     * Semaphore로 동시 요청 수를 제한하여 외부 API rate limit을 준수합니다.
     * 전체 수집이 4분 내에 완료되도록 타임아웃을 설정합니다.
     *
     * @return Pair(successCount, failCount)
     */
    private fun collectInParallel(indicators: List<Indicator>): Pair<Int, Int> {
        if (indicators.isEmpty()) return Pair(0, 0)

        val futures = indicators.map { indicator ->
            CompletableFuture.supplyAsync({
                try {
                    concurrencyLimit.acquire()
                    try {
                        collectLatestData(indicator)
                    } finally {
                        concurrencyLimit.release()
                    }
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    log.error("Collection interrupted for {}", indicator.symbol)
                    false
                }
            }, executor)
        }

        // 5분 배치 주기 내 완료를 보장하기 위해 4분 타임아웃
        val allDone = CompletableFuture.allOf(*futures.toTypedArray())
        try {
            allDone.get(4, TimeUnit.MINUTES)
        } catch (e: Exception) {
            log.warn("Parallel collection timed out or failed: {}", e.message)
        }

        val results = futures.map { future ->
            try {
                future.getNow(false)
            } catch (e: Exception) {
                false
            }
        }

        val successCount = results.count { it }
        val failCount = results.size - successCount
        return Pair(successCount, failCount)
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
