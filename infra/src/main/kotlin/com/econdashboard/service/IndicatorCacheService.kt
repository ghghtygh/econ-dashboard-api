package com.econdashboard.service

import com.econdashboard.config.RedisConfig.Companion.CACHE_INDICATORS
import com.econdashboard.config.RedisConfig.Companion.CACHE_INDICATOR_LATEST
import com.econdashboard.config.RedisConfig.Companion.CACHE_INDICATOR_SERIES
import com.econdashboard.domain.Indicator
import com.econdashboard.domain.IndicatorData
import com.econdashboard.enums.IndicatorCategory
import com.econdashboard.repository.IndicatorDataRepository
import com.econdashboard.repository.IndicatorRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class IndicatorCacheService(
    private val indicatorRepository: IndicatorRepository,
    private val indicatorDataRepository: IndicatorDataRepository,
    @Autowired(required = false)
    private val redisTemplate: RedisTemplate<String, Any>? = null
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Cacheable(cacheNames = [CACHE_INDICATORS], key = "'all'")
    fun findAllIndicators(): List<Indicator> {
        return indicatorRepository.findAll()
    }

    @Cacheable(cacheNames = [CACHE_INDICATORS], key = "'category:' + #category.name()")
    fun findByCategory(category: IndicatorCategory): List<Indicator> {
        return indicatorRepository.findByCategory(category)
    }

    @Cacheable(cacheNames = [CACHE_INDICATOR_LATEST], key = "#indicatorId")
    fun findLatestData(indicatorId: Long): IndicatorData? {
        return indicatorDataRepository.findTopByIndicatorIdOrderByDateDesc(indicatorId)
    }

    @Cacheable(
        cacheNames = [CACHE_INDICATOR_SERIES],
        key = "#indicatorId + ':' + #startDate + ':' + #endDate"
    )
    fun findSeriesData(indicatorId: Long, startDate: LocalDate, endDate: LocalDate): List<IndicatorData> {
        return indicatorDataRepository.findByIndicatorIdAndDateBetweenOrderByDateAsc(
            indicatorId, startDate, endDate
        )
    }

    @CacheEvict(cacheNames = [CACHE_INDICATORS], allEntries = true)
    fun evictIndicatorsCache() {
        // 지표 마스터 캐시 전체 초기화
    }

    @CacheEvict(cacheNames = [CACHE_INDICATOR_LATEST], key = "#indicatorId")
    fun evictLatestDataCache(indicatorId: Long) {
        // 특정 지표의 최신 데이터 캐시 초기화
    }

    @CacheEvict(cacheNames = [CACHE_INDICATOR_SERIES], allEntries = true)
    fun evictSeriesCache() {
        // 시계열 캐시 전체 초기화
    }

    /**
     * 특정 지표의 시리즈 캐시만 패턴 기반으로 삭제
     */
    fun evictSeriesCacheForIndicator(indicatorId: Long) {
        if (redisTemplate == null) {
            log.debug("RedisTemplate not available, skipping pattern-based cache eviction for indicator {}", indicatorId)
            return
        }
        val pattern = "$CACHE_INDICATOR_SERIES::$indicatorId:*"
        val keys = redisTemplate.keys(pattern)
        if (keys.isNotEmpty()) {
            redisTemplate.delete(keys)
            log.info("Evicted {} series cache entries for indicator {}", keys.size, indicatorId)
        }
    }

    fun evictAllCachesForIndicator(indicatorId: Long) {
        evictLatestDataCache(indicatorId)
        evictSeriesCacheForIndicator(indicatorId)
    }
}
