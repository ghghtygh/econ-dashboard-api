package com.econdashboard.client.common

import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.util.retry.Retry
import java.time.Duration

object RetryUtils {

    private val log = LoggerFactory.getLogger(RetryUtils::class.java)

    fun exponentialBackoff(maxAttempts: Long = 3): Retry {
        return Retry.backoff(maxAttempts, Duration.ofSeconds(1))
            .maxBackoff(Duration.ofSeconds(10))
            .filter { throwable ->
                when (throwable) {
                    is WebClientResponseException -> {
                        val status = throwable.statusCode.value()
                        status == 429 || status >= 500
                    }
                    else -> true
                }
            }
            .doBeforeRetry { signal ->
                log.warn(
                    "Retrying external API call (attempt {}): {}",
                    signal.totalRetries() + 1,
                    signal.failure().message
                )
            }
    }
}
