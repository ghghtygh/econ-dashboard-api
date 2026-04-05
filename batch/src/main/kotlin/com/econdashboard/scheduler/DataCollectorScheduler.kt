package com.econdashboard.scheduler

import com.econdashboard.collector.CollectionAlertService
import com.econdashboard.collector.CollectorHealthRegistry
import com.econdashboard.collector.CryptoCollector
import com.econdashboard.collector.MacroCollector
import com.econdashboard.collector.MarketCollector
import com.econdashboard.collector.NewsCollector
import com.econdashboard.service.CacheEvictionListener
import org.slf4j.LoggerFactory
import org.springframework.retry.support.RetryTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class DataCollectorScheduler(
    private val cryptoCollector: CryptoCollector,
    private val macroCollector: MacroCollector,
    private val marketCollector: MarketCollector,
    private val newsCollector: NewsCollector,
    private val cacheEvictionListener: CacheEvictionListener,
    private val collectorRetryTemplate: RetryTemplate,
    private val healthRegistry: CollectorHealthRegistry,
    private val alertService: CollectionAlertService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 암호화폐 수집 - 매 5분
     */
    @Scheduled(fixedRate = 5 * 60 * 1000, initialDelay = 10_000)
    fun collectCryptoData() {
        executeWithRetry("crypto") {
            val count = cryptoCollector.collect()
            cacheEvictionListener.onBatchCollectionCompleted()
            count
        }
    }

    /**
     * 주식/환율 수집 - 매 15분
     */
    @Scheduled(fixedRate = 15 * 60 * 1000, initialDelay = 15_000)
    fun collectMarketData() {
        executeWithRetry("market-frequent") {
            val count = marketCollector.collectFrequent()
            cacheEvictionListener.onBatchCollectionCompleted()
            count
        }
    }

    /**
     * 원자재/채권 수집 - 매 1시간
     */
    @Scheduled(fixedRate = 60 * 60 * 1000, initialDelay = 20_000)
    fun collectCommodityData() {
        executeWithRetry("market-hourly") {
            val count = marketCollector.collectHourly()
            cacheEvictionListener.onBatchCollectionCompleted()
            count
        }
    }

    /**
     * 거시경제 지표 수집 (FRED) - 매 6시간
     */
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000, initialDelay = 30_000)
    fun collectMacroData() {
        executeWithRetry("macro") {
            val count = macroCollector.collect()
            cacheEvictionListener.onBatchCollectionCompleted()
            count
        }
    }

    /**
     * 경제 뉴스 수집 - 매 30분
     */
    @Scheduled(fixedRate = 30 * 60 * 1000, initialDelay = 25_000)
    fun collectNewsData() {
        executeWithRetry("news") {
            newsCollector.collect()
            0
        }
    }

    private fun executeWithRetry(collectorName: String, task: () -> Int) {
        log.info("[Scheduler] {} collection started", collectorName)
        try {
            val itemCount = collectorRetryTemplate.execute<Int, Exception> { context ->
                if (context.retryCount > 0) {
                    log.warn("[Scheduler] {} collection retry attempt #{}", collectorName, context.retryCount)
                }
                task()
            }
            healthRegistry.recordSuccess(collectorName, itemCount)
            log.info("[Scheduler] {} collection completed successfully ({} items)", collectorName, itemCount)
        } catch (e: Exception) {
            log.error("[Scheduler] {} collection failed after retries: {}", collectorName, e.message, e)
            healthRegistry.recordFailure(collectorName, e)
            alertService.checkAndAlert(collectorName)
        }
    }
}
