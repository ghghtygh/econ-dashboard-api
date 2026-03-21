package com.econdashboard.collector

import com.econdashboard.enums.DataSource
import com.econdashboard.enums.IndicatorCategory
import com.econdashboard.service.DataCollectionService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class MarketCollector(
    private val dataCollectionService: DataCollectionService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        val FREQUENT_CATEGORIES = listOf(IndicatorCategory.STOCK, IndicatorCategory.FOREX)
        val HOURLY_CATEGORIES = listOf(IndicatorCategory.COMMODITY, IndicatorCategory.BOND)
    }

    fun collectFrequent() {
        log.info("Starting frequent market data collection")
        dataCollectionService.collectBySourceAndCategories(DataSource.YAHOO, FREQUENT_CATEGORIES)
    }

    fun collectHourly() {
        log.info("Starting hourly market data collection")
        dataCollectionService.collectBySourceAndCategories(DataSource.YAHOO, HOURLY_CATEGORIES)
    }
}
