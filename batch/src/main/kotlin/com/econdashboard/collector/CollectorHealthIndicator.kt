package com.econdashboard.collector

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

@Component
class CollectorHealthIndicator(
    private val healthRegistry: CollectorHealthRegistry
) : HealthIndicator {

    override fun health(): Health {
        val allStatuses = healthRegistry.getAllStatuses()

        if (allStatuses.isEmpty()) {
            return Health.unknown().withDetail("message", "No collectors have run yet").build()
        }

        val unhealthy = healthRegistry.getUnhealthyCollectors()

        if (unhealthy.isEmpty()) {
            return Health.up()
                .withDetail("collectors", allStatuses.mapValues { (_, s) -> toDetail(s) })
                .build()
        }

        return Health.down()
            .withDetail("unhealthy", unhealthy.map { it.name })
            .withDetail("collectors", allStatuses.mapValues { (_, s) -> toDetail(s) })
            .build()
    }

    private fun toDetail(status: CollectorStatus): Map<String, Any?> = mapOf(
        "healthy" to status.healthy,
        "consecutiveFailures" to status.consecutiveFailures,
        "lastSuccessAt" to status.lastSuccessAt?.toString(),
        "lastFailureAt" to status.lastFailureAt?.toString(),
        "lastError" to status.lastErrorMessage,
        "totalSuccesses" to status.totalSuccesses,
        "totalFailures" to status.totalFailures
    )
}
