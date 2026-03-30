package com.econdashboard.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
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
@ConditionalOnBean(RedisConnectionFactory::class)
class RedisConfig {

    companion object {
        const val CACHE_INDICATORS = "indicators"
        const val CACHE_INDICATOR_LATEST = "indicator:latest"
        const val CACHE_INDICATOR_SERIES = "indicator:series"
    }

    private fun redisObjectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerModule(JavaTimeModule())
            registerModule(KotlinModule.Builder().build())
            activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
            )
        }
    }

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val jsonSerializer = GenericJackson2JsonRedisSerializer(redisObjectMapper())
        return RedisTemplate<String, Any>().apply {
            this.connectionFactory = connectionFactory
            keySerializer = StringRedisSerializer()
            valueSerializer = jsonSerializer
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = jsonSerializer
        }
    }

    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): RedisCacheManager {
        val jsonSerializer = GenericJackson2JsonRedisSerializer(redisObjectMapper())
        val keySerializer = StringRedisSerializer()

        val defaultConfig = createCacheConfig(keySerializer, jsonSerializer, Duration.ofMinutes(6))

        // TTL을 배치 주기(5분)보다 약간 길게(6분) 설정하여,
        // 배치 완료 → 캐시 evict 사이 불필요한 TTL 만료를 방지
        val cacheConfigs = mapOf(
            CACHE_INDICATORS to createCacheConfig(keySerializer, jsonSerializer, Duration.ofHours(1)),
            CACHE_INDICATOR_LATEST to createCacheConfig(keySerializer, jsonSerializer, Duration.ofMinutes(6)),
            CACHE_INDICATOR_SERIES to createCacheConfig(keySerializer, jsonSerializer, Duration.ofMinutes(6))
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
