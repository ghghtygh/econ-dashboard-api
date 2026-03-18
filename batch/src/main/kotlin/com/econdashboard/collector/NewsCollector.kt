package com.econdashboard.collector

import com.econdashboard.service.NewsCollectionService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class NewsCollector(
    private val newsCollectionService: NewsCollectionService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun collect() {
        log.info("Starting news collection")
        val count = newsCollectionService.collectNews()
        log.info("News collection completed: {} articles collected", count)
    }
}
