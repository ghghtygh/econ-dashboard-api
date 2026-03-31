package com.econdashboard.controller

import com.econdashboard.collector.CollectorHealthRegistry
import com.econdashboard.collector.CollectorStatus
import com.econdashboard.scheduler.HistoricalBackfillRunner
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.LocalDate

@RestController
@RequestMapping("/api/collectors")
class CollectorStatusController(
    private val healthRegistry: CollectorHealthRegistry,
    private val historicalBackfillRunner: HistoricalBackfillRunner
) {

    @GetMapping("/status")
    fun getStatus(): CollectorStatusResponse {
        val statuses = healthRegistry.getAllStatuses()
        return CollectorStatusResponse(
            timestamp = Instant.now(),
            healthy = !healthRegistry.hasUnhealthyCollector(),
            collectors = statuses.mapValues { (_, s) -> toDto(s) }
        )
    }

    /**
     * 이력 데이터 backfill 수동 트리거.
     * from/to 미지정 시 기본값: 최근 2년
     */
    @PostMapping("/backfill")
    fun triggerBackfill(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?
    ): BackfillTriggerResponse {
        val end = to ?: LocalDate.now()
        val start = from ?: end.minusYears(2)
        historicalBackfillRunner.runBackfill(from = start, to = end, minDataPoints = 0)
        return BackfillTriggerResponse(
            message = "Backfill triggered for period $start ~ $end",
            from = start,
            to = end
        )
    }

    private fun toDto(status: CollectorStatus) = CollectorStatusDto(
        name = status.name,
        healthy = status.healthy,
        consecutiveFailures = status.consecutiveFailures,
        lastSuccessAt = status.lastSuccessAt,
        lastFailureAt = status.lastFailureAt,
        lastErrorMessage = status.lastErrorMessage,
        totalSuccesses = status.totalSuccesses,
        totalFailures = status.totalFailures,
        lastCollectionCount = status.lastCollectionCount
    )
}

data class CollectorStatusResponse(
    val timestamp: Instant,
    val healthy: Boolean,
    val collectors: Map<String, CollectorStatusDto>
)

data class CollectorStatusDto(
    val name: String,
    val healthy: Boolean,
    val consecutiveFailures: Int,
    val lastSuccessAt: Instant?,
    val lastFailureAt: Instant?,
    val lastErrorMessage: String?,
    val totalSuccesses: Long,
    val totalFailures: Long,
    val lastCollectionCount: Int
)

data class BackfillTriggerResponse(
    val message: String,
    val from: LocalDate,
    val to: LocalDate
)
