package com.econdashboard.service

import com.econdashboard.event.IndicatorDataCollectedEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class CacheEvictionListener(
    private val indicatorCacheService: IndicatorCacheService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    fun onIndicatorDataCollected(event: IndicatorDataCollectedEvent) {
        log.info("Cache refresh triggered for indicator: {}", event.indicatorId)
        indicatorCacheService.evictAllCachesForIndicator(event.indicatorId)
    }
}
