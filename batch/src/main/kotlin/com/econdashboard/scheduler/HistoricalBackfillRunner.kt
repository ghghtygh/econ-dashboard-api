package com.econdashboard.scheduler

import com.econdashboard.service.DataCollectionService
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * 앱 시작 시 이력 데이터 backfill을 수행합니다.
 * 데이터 포인트가 부족한 지표에 대해 과거 2년치 일별 데이터를 수집합니다.
 */
@Component
class HistoricalBackfillRunner(
    private val dataCollectionService: DataCollectionService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Async
    @EventListener(ApplicationReadyEvent::class)
    fun runBackfillOnStartup() {
        val to = LocalDate.now()
        val from = to.minusYears(2)

        log.info("[Backfill] Starting historical backfill on application startup (from={}, to={})", from, to)
        try {
            dataCollectionService.backfillAllIndicators(from = from, to = to, minDataPoints = 20)
            log.info("[Backfill] Startup historical backfill completed")
        } catch (e: Exception) {
            log.error("[Backfill] Startup historical backfill failed: {}", e.message, e)
        }
    }

    /**
     * 특정 기간에 대한 backfill을 수동으로 트리거합니다.
     */
    fun runBackfill(from: LocalDate, to: LocalDate, minDataPoints: Int = 0) {
        log.info("[Backfill] Manual backfill triggered (from={}, to={}, minDataPoints={})", from, to, minDataPoints)
        dataCollectionService.backfillAllIndicators(from = from, to = to, minDataPoints = minDataPoints)
        log.info("[Backfill] Manual backfill completed")
    }
}
