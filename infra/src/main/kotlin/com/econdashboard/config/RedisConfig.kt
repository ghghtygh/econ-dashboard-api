package com.econdashboard.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableCaching
class RedisConfig {

    companion object {
        const val CACHE_INDICATORS = "indicators"
        const val CACHE_INDICATOR_LATEST = "indicator:latest"
        const val CACHE_INDICATOR_SERIES = "indicator:series"
    }

    @Bean
    @ConditionalOnBean(RedisConnectionFactory::class)
    fun redisTemplate(connectionFactory: RedisConnectionFactory, objectMapper: ObjectMapper): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            this.connectionFactory = connectionFactory
            keySerializer = StringRedisSerializer()
            valueSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
        }
    }

    @Bean
    @ConditionalOnBean(RedisConnectionFactory::class)
    fun cacheManager(connectionFactory: RedisConnectionFactory, objectMapper: ObjectMapper): RedisCacheManager {
        val jsonSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
        val keySerializer = StringRedisSerializer()

        val defaultConfig = createCacheConfig(keySerializer, jsonSerializer, Duration.ofMinutes(5))

        val cacheConfigs = mapOf(
            CACHE_INDICATORS to createCacheConfig(keySerializer, jsonSerializer, Duration.ofHours(1)),
            CACHE_INDICATOR_LATEST to createCacheConfig(keySerializer, jsonSerializer, Duration.ofMinutes(5)),
            CACHE_INDICATOR_SERIES to createCacheConfig(keySerializer, jsonSerializer, Duration.ofMinutes(5))
        )

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigs)
            .build()
    }

    private fun createCacheConfig(
        keySerializer: StringRedisSerializer,
        valueSerializer: GenericJackson2JsonRedisSerializer,
        ttl: Duration
    ): RedisCacheConfiguration {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(ttl)
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
    }
}
