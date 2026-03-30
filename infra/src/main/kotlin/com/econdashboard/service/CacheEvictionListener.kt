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
        log.debug("Evicting caches for indicator: {}", event.indicatorId)
        indicatorCacheService.evictAllCachesForIndicator(event.indicatorId)
    }

    /**
     * 배치 수집 사이클 완료 후 호출하여 전체 캐시를 명시적으로 갱신합니다.
     * 이를 통해 캐시 TTL과 배치 주기 간 타이밍 불일치 문제를 해결합니다.
     */
    fun onBatchCollectionCompleted() {
        log.info("Batch collection completed — evicting all caches for fresh data")
        indicatorCacheService.evictIndicatorsCache()
        indicatorCacheService.evictSeriesCache()
    }
}
