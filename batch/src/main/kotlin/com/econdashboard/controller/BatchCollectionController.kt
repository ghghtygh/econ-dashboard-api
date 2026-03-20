package com.econdashboard.controller

import com.econdashboard.collector.CryptoCollector
import com.econdashboard.collector.MarketCollector
import com.econdashboard.collector.NewsCollector
import com.econdashboard.service.DataCollectionService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/batch/collect")
class BatchCollectionController(
    private val dataCollectionService: DataCollectionService,
    private val cryptoCollector: CryptoCollector,
    private val marketCollector: MarketCollector,
    private val newsCollector: NewsCollector
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/all")
    fun collectAll(): ResponseEntity<Map<String, Int>> {
        log.info("[Manual] Full data collection triggered")
        val result = dataCollectionService.collectAll()
        return ResponseEntity.ok(result)
    }

    @PostMapping("/crypto")
    fun collectCrypto(): ResponseEntity<Map<String, String>> {
        log.info("[Manual] Crypto collection triggered")
        cryptoCollector.collect()
        return ResponseEntity.ok(mapOf("status" to "completed"))
    }

    @PostMapping("/market")
    fun collectMarket(): ResponseEntity<Map<String, String>> {
        log.info("[Manual] Market collection triggered")
        marketCollector.collectFrequent()
        return ResponseEntity.ok(mapOf("status" to "completed"))
    }

    @PostMapping("/commodity")
    fun collectCommodity(): ResponseEntity<Map<String, String>> {
        log.info("[Manual] Commodity collection triggered")
        marketCollector.collectHourly()
        return ResponseEntity.ok(mapOf("status" to "completed"))
    }

    @PostMapping("/news")
    fun collectNews(): ResponseEntity<Map<String, String>> {
        log.info("[Manual] News collection triggered")
        newsCollector.collect()
        return ResponseEntity.ok(mapOf("status" to "completed"))
    }
}
