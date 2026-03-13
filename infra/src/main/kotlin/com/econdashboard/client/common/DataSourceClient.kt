package com.econdashboard.client.common

import com.econdashboard.enums.DataSource
import java.math.BigDecimal
import java.time.LocalDate

interface DataSourceClient {

    val source: DataSource

    fun fetchLatestPrice(symbol: String): ExternalDataPoint?

    fun fetchHistoricalData(symbol: String, from: LocalDate, to: LocalDate): List<ExternalDataPoint>
}

data class ExternalDataPoint(
    val date: LocalDate,
    val value: BigDecimal,
    val open: BigDecimal? = null,
    val high: BigDecimal? = null,
    val low: BigDecimal? = null,
    val close: BigDecimal? = null,
    val volume: BigDecimal? = null,
    val change: BigDecimal? = null
)
