package com.econdashboard.collector

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class CollectionAlertService(
    private val healthRegistry: CollectorHealthRegistry,
    @Value("\${collector.alert.webhook-url:}") private val webhookUrl: String,
    @Value("\${collector.alert.failure-threshold:3}") private val failureThreshold: Int
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.create()

    fun checkAndAlert(collectorName: String) {
        val status = healthRegistry.getStatus(collectorName) ?: return

        if (status.consecutiveFailures < failureThreshold) return

        // 임계값 도달 시점에만 알림 (반복 알림 방지)
        if (status.consecutiveFailures == failureThreshold) {
            sendAlert(collectorName, status)
        }
    }

    private fun sendAlert(collectorName: String, status: CollectorStatus) {
        val message = buildAlertMessage(collectorName, status)
        log.error("[ALERT] {}", message)

        if (webhookUrl.isNotBlank()) {
            sendWebhookAlert(message)
        }
    }

    private fun buildAlertMessage(collectorName: String, status: CollectorStatus): String {
        return """
            Collector '$collectorName' has failed ${status.consecutiveFailures} consecutive times.
            Last error: ${status.lastErrorMessage}
            Last success: ${status.lastSuccessAt ?: "never"}
            Last failure: ${status.lastFailureAt}
        """.trimIndent()
    }

    private fun sendWebhookAlert(message: String) {
        try {
            restClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapOf("text" to message))
                .retrieve()
                .toBodilessEntity()
            log.info("Alert webhook sent successfully")
        } catch (e: Exception) {
            log.error("Failed to send alert webhook: {}", e.message)
        }
    }
}
