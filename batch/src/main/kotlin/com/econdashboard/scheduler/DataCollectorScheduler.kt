package com.econdashboard.scheduler

import com.econdashboard.collector.CryptoCollector
import com.econdashboard.collector.MarketCollector
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class DataCollectorScheduler(
    private val cryptoCollector: CryptoCollector,
    private val marketCollector: MarketCollector
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
        } catch (e: Exception) {
            log.error("[Scheduler] Market (hourly) collection failed: {}", e.message, e)
        }
    }
}
