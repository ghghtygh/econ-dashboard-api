package com.econdashboard.collector

import com.econdashboard.service.DataCollectionService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CryptoCollector(
    private val dataCollectionService: DataCollectionService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        val CRYPTO_SYMBOLS = listOf("bitcoin", "ethereum")
    }

    fun collect(): Int {
        log.info("Starting crypto data collection for {} symbols", CRYPTO_SYMBOLS.size)
        return dataCollectionService.collectBySymbols(CRYPTO_SYMBOLS)
    }
}
