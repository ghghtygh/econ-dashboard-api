package com.econdashboard.collector

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

data class CollectorStatus(
    val name: String,
    val lastSuccessAt: Instant? = null,
    val lastFailureAt: Instant? = null,
    val lastErrorMessage: String? = null,
    val consecutiveFailures: Int = 0,
    val totalSuccesses: Long = 0,
    val totalFailures: Long = 0,
    val lastCollectionCount: Int = 0
) {
    val healthy: Boolean get() = consecutiveFailures < FAILURE_THRESHOLD

    companion object {
        const val FAILURE_THRESHOLD = 3
        const val REDIS_KEY = "collector:status"
        val REDIS_TTL: Duration = Duration.ofHours(1)
    }
}

@Component
class CollectorHealthRegistry(
    private val redisTemplate: StringRedisTemplate?
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val statuses = ConcurrentHashMap<String, CollectorStatus>()
    private val objectMapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
        registerModule(KotlinModule.Builder().build())
    }

    fun recordSuccess(collectorName: String, itemCount: Int = 0) {
        statuses.compute(collectorName) { _, current ->
            (current ?: CollectorStatus(name = collectorName)).copy(
                lastSuccessAt = Instant.now(),
                consecutiveFailures = 0,
                totalSuccesses = (current?.totalSuccesses ?: 0) + 1,
                lastCollectionCount = itemCount
            )
        }
        persistToRedis()
    }

    fun recordFailure(collectorName: String, error: Exception) {
        statuses.compute(collectorName) { _, current ->
            (current ?: CollectorStatus(name = collectorName)).copy(
                lastFailureAt = Instant.now(),
                lastErrorMessage = error.message,
                consecutiveFailures = (current?.consecutiveFailures ?: 0) + 1,
                totalFailures = (current?.totalFailures ?: 0) + 1
            )
        }
        persistToRedis()
    }

    fun getStatus(collectorName: String): CollectorStatus? = statuses[collectorName]

    fun getAllStatuses(): Map<String, CollectorStatus> = statuses.toMap()

    fun hasUnhealthyCollector(): Boolean = statuses.values.any { !it.healthy }

    fun getUnhealthyCollectors(): List<CollectorStatus> = statuses.values.filter { !it.healthy }

    private fun persistToRedis() {
        if (redisTemplate == null) return
        try {
            val json = objectMapper.writeValueAsString(statuses.toMap())
            redisTemplate.opsForValue().set(CollectorStatus.REDIS_KEY, json, CollectorStatus.REDIS_TTL)
        } catch (e: Exception) {
            log.warn("[CollectorHealthRegistry] Failed to persist status to Redis: {}", e.message)
        }
    }
}
