package com.econdashboard.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.backoff.ExponentialBackOffPolicy
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate

@Configuration
class RetryConfig(
    @Value("\${collector.retry.max-attempts:3}") private val maxAttempts: Int,
    @Value("\${collector.retry.initial-interval-ms:2000}") private val initialInterval: Long,
    @Value("\${collector.retry.multiplier:2.0}") private val multiplier: Double,
    @Value("\${collector.retry.max-interval-ms:10000}") private val maxInterval: Long
) {

    @Bean
    fun collectorRetryTemplate(): RetryTemplate {
        return RetryTemplate().apply {
            setRetryPolicy(SimpleRetryPolicy(maxAttempts))
            setBackOffPolicy(ExponentialBackOffPolicy().apply {
                initialInterval = this@RetryConfig.initialInterval
                multiplier = this@RetryConfig.multiplier
                maxInterval = this@RetryConfig.maxInterval
            })
        }
    }
}
