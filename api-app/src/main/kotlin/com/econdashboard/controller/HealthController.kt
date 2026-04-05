package com.econdashboard.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import javax.sql.DataSource

@Tag(name = "Health", description = "서버 상태 확인 API")
@RestController
@RequestMapping("/api/health")
class HealthController(
    private val dataSource: DataSource,
    private val redisConnectionFactory: RedisConnectionFactory?
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Operation(summary = "서버 상태 확인", description = "데이터베이스, Redis 등 연결 상태를 확인합니다")
    @GetMapping
    fun health(): HealthResponse {
        val dbStatus = checkDatabase()
        val redisStatus = checkRedis()

        return HealthResponse(
            status = if (dbStatus && redisStatus) "ok" else "degraded",
            timestamp = Instant.now(),
            dataSources = DataSourceStatus(
                database = dbStatus,
                redis = redisStatus
            )
        )
    }

    private fun checkDatabase(): Boolean {
        return try {
            dataSource.connection.use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery("SELECT 1").close()
                }
            }
            true
        } catch (e: Exception) {
            log.warn("[Health] Database check failed: {}", e.message)
            false
        }
    }

    private fun checkRedis(): Boolean {
        if (redisConnectionFactory == null) return false
        return try {
            val conn = redisConnectionFactory.connection
            try {
                val pong = conn.ping()
                pong != null && pong.isNotEmpty()
            } finally {
                conn.close()
            }
        } catch (e: Exception) {
            log.warn("[Health] Redis check failed: {}", e.message)
            false
        }
    }
}

data class HealthResponse(
    val status: String,
    val timestamp: Instant,
    val dataSources: DataSourceStatus
)

data class DataSourceStatus(
    val database: Boolean,
    val redis: Boolean
)
