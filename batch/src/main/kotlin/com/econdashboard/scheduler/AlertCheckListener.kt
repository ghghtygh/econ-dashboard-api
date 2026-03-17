package com.econdashboard.scheduler

import com.econdashboard.event.IndicatorDataCollectedEvent
import com.econdashboard.service.AlertService
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class AlertCheckListener(
    private val alertService: AlertService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    fun onIndicatorDataCollected(event: IndicatorDataCollectedEvent) {
        try {
            alertService.checkAlerts(event.indicatorId)
        } catch (e: Exception) {
            log.error("Failed to check alerts for indicator {}: {}", event.indicatorId, e.message)
        }
    }
}
