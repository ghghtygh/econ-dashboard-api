package com.econdashboard.dto

import java.math.BigDecimal
import java.time.LocalDate

data class CrisisPreset(
    val id: String,
    val name: String,
    val description: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class ComparisonRequest(
    val indicatorId: Long,
    val currentStart: LocalDate,
    val currentEnd: LocalDate,
    val historicalStart: LocalDate,
    val historicalEnd: LocalDate
)

data class NormalizedDataPoint(
    val dayOffset: Int,
    val date: LocalDate,
    val value: BigDecimal,
    val normalizedValue: BigDecimal
)

data class ComparisonSeriesResponse(
    val indicatorId: Long,
    val indicatorName: String,
    val currentPeriod: List<NormalizedDataPoint>,
    val historicalPeriod: List<NormalizedDataPoint>,
    val currentStartDate: LocalDate,
    val historicalStartDate: LocalDate
)
