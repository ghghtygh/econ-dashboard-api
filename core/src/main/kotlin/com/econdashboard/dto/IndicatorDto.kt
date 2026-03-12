package com.econdashboard.dto

import com.econdashboard.domain.Indicator
import com.econdashboard.domain.IndicatorData
import com.econdashboard.enums.DataSource
import com.econdashboard.enums.IndicatorCategory
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class IndicatorResponse(
    val id: Long,
    val name: String,
    val symbol: String,
    val category: IndicatorCategory,
    val unit: String,
    val source: DataSource,
    val description: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(indicator: Indicator) = IndicatorResponse(
            id = indicator.id,
            name = indicator.name,
            symbol = indicator.symbol,
            category = indicator.category,
            unit = indicator.unit,
            source = indicator.source,
            description = indicator.description,
            createdAt = indicator.createdAt,
            updatedAt = indicator.updatedAt
        )
    }
}

data class IndicatorDataResponse(
    val id: Long,
    val indicatorId: Long,
    val date: LocalDate,
    val value: BigDecimal,
    val open: BigDecimal?,
    val high: BigDecimal?,
    val low: BigDecimal?,
    val close: BigDecimal?,
    val volume: BigDecimal?,
    val change: BigDecimal?
) {
    companion object {
        fun from(data: IndicatorData) = IndicatorDataResponse(
            id = data.id,
            indicatorId = data.indicator.id,
            date = data.date,
            value = data.value,
            open = data.open,
            high = data.high,
            low = data.low,
            close = data.close,
            volume = data.volume,
            change = data.change
        )
    }
}

data class IndicatorSeriesRequest(
    val indicatorId: Long,
    val startDate: LocalDate?,
    val endDate: LocalDate?
)
