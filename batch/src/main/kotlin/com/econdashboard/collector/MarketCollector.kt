package com.econdashboard.collector

import com.econdashboard.service.DataCollectionService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class MarketCollector(
    private val dataCollectionService: DataCollectionService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        // 주식/환율 - 매 5분 수집
        val FREQUENT_SYMBOLS = listOf("^GSPC", "^IXIC", "^KS11", "^DJI", "^RUT", "^KQ11", "^VIX", "USDKRW=X", "EURUSD=X")

        // 원자재/채권 - 매 1시간 수집
        val HOURLY_SYMBOLS = listOf("GC=F", "CL=F", "NG=F", "HG=F", "ZW=F", "ZS=F", "^TNX", "^IRX")
    }

    fun collectFrequent() {
        log.info("Starting frequent market data collection for {} symbols", FREQUENT_SYMBOLS.size)
        dataCollectionService.collectBySymbols(FREQUENT_SYMBOLS)
    }

    fun collectHourly() {
        log.info("Starting hourly market data collection for {} symbols", HOURLY_SYMBOLS.size)
        dataCollectionService.collectBySymbols(HOURLY_SYMBOLS)
    }
}
