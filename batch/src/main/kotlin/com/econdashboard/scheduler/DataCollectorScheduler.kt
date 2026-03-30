package com.econdashboard.scheduler

import com.econdashboard.collector.CryptoCollector
import com.econdashboard.collector.MacroCollector
import com.econdashboard.collector.MarketCollector
import com.econdashboard.collector.NewsCollector
import com.econdashboard.service.CacheEvictionListener
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class DataCollectorScheduler(
    private val cryptoCollector: CryptoCollector,
    private val macroCollector: MacroCollector,
    private val marketCollector: MarketCollector,
    private val newsCollector: NewsCollector,
    private val cacheEvictionListener: CacheEvictionListener
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 암호화폐 수집 - 매 5분
     */
    @Scheduled(fixedRate = 5 * 60 * 1000, initialDelay = 10_000)
    fun collectCryptoData() {
        log.info("[Scheduler] Crypto collection started")
        try {
            cryptoCollector.collect()
            cacheEvictionListener.onBatchCollectionCompleted()
        } catch (e: Exception) {
            log.error("[Scheduler] Crypto collection failed: {}", e.message, e)
        }
    }

    /**
     * 주식/환율 수집 - 매 5분
     */
    @Scheduled(fixedRate = 5 * 60 * 1000, initialDelay = 15_000)
    fun collectMarketData() {
        log.info("[Scheduler] Market (frequent) collection started")
        try {
            marketCollector.collectFrequent()
            cacheEvictionListener.onBatchCollectionCompleted()
        } catch (e: Exception) {
            log.error("[Scheduler] Market (frequent) collection failed: {}", e.message, e)
        }
    }

    /**
     * 원자재/채권 수집 - 매 1시간
     */
    @Scheduled(fixedRate = 60 * 60 * 1000, initialDelay = 20_000)
    fun collectCommodityData() {
        log.info("[Scheduler] Market (hourly) collection started")
        try {
            marketCollector.collectHourly()
            cacheEvictionListener.onBatchCollectionCompleted()
        } catch (e: Exception) {
            log.error("[Scheduler] Market (hourly) collection failed: {}", e.message, e)
        }
    }

    /**
     * 거시경제 지표 수집 (FRED) - 매 6시간
     * CPI, 실업률, PCE 등은 월간 데이터이므로 하루 4회면 충분
     */
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000, initialDelay = 30_000)
    fun collectMacroData() {
        log.info("[Scheduler] Macro (FRED) collection started")
        try {
            macroCollector.collect()
            cacheEvictionListener.onBatchCollectionCompleted()
        } catch (e: Exception) {
            log.error("[Scheduler] Macro (FRED) collection failed: {}", e.message, e)
        }
    }

    /**
     * 경제 뉴스 수집 - 매 30분
     */
    @Scheduled(fixedRate = 30 * 60 * 1000, initialDelay = 25_000)
    fun collectNewsData() {
        log.info("[Scheduler] News collection started")
        try {
            newsCollector.collect()
        } catch (e: Exception) {
            log.error("[Scheduler] News collection failed: {}", e.message, e)
        }
    }
}
