package com.econdashboard.collector

import com.econdashboard.enums.DataSource
import com.econdashboard.service.DataCollectionService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CryptoCollector(
    private val dataCollectionService: DataCollectionService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun collect() {
        log.info("Starting crypto data collection")
        dataCollectionService.collectBySource(DataSource.COINGECKO)
    }
}
