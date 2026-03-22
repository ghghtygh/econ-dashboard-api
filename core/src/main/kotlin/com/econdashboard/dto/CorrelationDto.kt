package com.econdashboard.dto

import java.math.BigDecimal
import java.time.LocalDate

data class CorrelationRequest(
    val indicatorIds: List<Long>,
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class CorrelationPairResponse(
    val indicatorAId: Long,
    val indicatorAName: String,
    val indicatorBId: Long,
    val indicatorBName: String,
    val correlation: BigDecimal,
    val sampleCount: Int
)

data class CorrelationMatrixResponse(
    val indicatorIds: List<Long>,
    val indicatorNames: List<String>,
    val matrix: List<List<BigDecimal>>,
    val sampleCounts: List<List<Int>>,
    val startDate: LocalDate,
    val endDate: LocalDate
)
