package com.econdashboard.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@Tag(name = "Collectors", description = "데이터 수집기 상태 API")
@RestController
@RequestMapping("/api/collectors")
class CollectorStatusController(
    private val redisTemplate: StringRedisTemplate
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val objectMapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
        registerModule(KotlinModule.Builder().build())
    }

    @Operation(summary = "수집기 상태 조회", description = "배치 수집기들의 현재 상태를 조회합니다")
    @GetMapping("/status")
    fun getStatus(): CollectorStatusResponse {
        val statuses = readFromRedis()

        if (statuses.isEmpty()) {
            return CollectorStatusResponse(
                timestamp = Instant.now(),
                healthy = true,
                collectors = emptyMap(),
                message = "수집기 상태 정보가 아직 없습니다. 배치가 시작되면 자동 갱신됩니다."
            )
        }

        val hasUnhealthy = statuses.values.any { !it.healthy }
        return CollectorStatusResponse(
            timestamp = Instant.now(),
            healthy = !hasUnhealthy,
            collectors = statuses
        )
    }

    private fun readFromRedis(): Map<String, CollectorStatusDto> {
        return try {
            val json = redisTemplate.opsForValue().get(REDIS_KEY) ?: return emptyMap()
            objectMapper.readValue(json, object : TypeReference<Map<String, CollectorStatusDto>>() {})
        } catch (e: Exception) {
            log.warn("[CollectorStatus] Failed to read from Redis: {}", e.message)
            emptyMap()
        }
    }

    companion object {
        const val REDIS_KEY = "collector:status"
    }
}

data class CollectorStatusResponse(
    val timestamp: Instant,
    val healthy: Boolean,
    val collectors: Map<String, CollectorStatusDto>,
    val message: String? = null
)

data class CollectorStatusDto(
    val name: String = "",
    val healthy: Boolean = true,
    val consecutiveFailures: Int = 0,
    val lastSuccessAt: Instant? = null,
    val lastFailureAt: Instant? = null,
    val lastErrorMessage: String? = null,
    val totalSuccesses: Long = 0,
    val totalFailures: Long = 0,
    val lastCollectionCount: Int = 0
)
