package com.econdashboard.service

import com.econdashboard.dto.CorrelationMatrixResponse
import com.econdashboard.dto.CorrelationPairResponse
import com.econdashboard.exception.NotFoundException
import com.econdashboard.repository.IndicatorDataRepository
import com.econdashboard.repository.IndicatorRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import kotlin.math.sqrt

@Service
@Transactional(readOnly = true)
class CorrelationService(
    private val indicatorRepository: IndicatorRepository,
    private val indicatorDataRepository: IndicatorDataRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun getCorrelationPairs(
        indicatorIds: List<Long>,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<CorrelationPairResponse> {
        val indicators = indicatorIds.map { id ->
            indicatorRepository.findById(id).orElseThrow { NotFoundException("Indicator", id) }
        }

        val dataMap = indicators.associate { indicator ->
            indicator.id to getReturnsByDate(indicator.id, startDate, endDate)
        }

        val pairs = mutableListOf<CorrelationPairResponse>()

        for (i in indicators.indices) {
            for (j in i + 1 until indicators.size) {
                val a = indicators[i]
                val b = indicators[j]
                val (alignedA, alignedB) = alignByDate(dataMap[a.id]!!, dataMap[b.id]!!)

                if (alignedA.size >= 2) {
                    val corr = pearsonCorrelation(alignedA, alignedB)
                    pairs.add(
                        CorrelationPairResponse(
                            indicatorAId = a.id,
                            indicatorAName = a.name,
                            indicatorBId = b.id,
                            indicatorBName = b.name,
                            correlation = corr.toBigDecimal().setScale(4, RoundingMode.HALF_UP),
                            sampleCount = alignedA.size
                        )
                    )
                }
            }
        }

        return pairs
    }

    fun getCorrelationMatrix(
        indicatorIds: List<Long>,
        startDate: LocalDate,
        endDate: LocalDate
    ): CorrelationMatrixResponse {
        val indicators = indicatorIds.map { id ->
            indicatorRepository.findById(id).orElseThrow { NotFoundException("Indicator", id) }
        }

        val dataMap = indicators.associate { indicator ->
            indicator.id to getReturnsByDate(indicator.id, startDate, endDate)
        }

        val n = indicators.size
        val matrix = Array(n) { Array(n) { BigDecimal.ZERO } }
        val sampleCounts = Array(n) { IntArray(n) }

        for (i in 0 until n) {
            matrix[i][i] = BigDecimal.ONE
            sampleCounts[i][i] = dataMap[indicators[i].id]?.size ?: 0

            for (j in i + 1 until n) {
                val (alignedA, alignedB) = alignByDate(
                    dataMap[indicators[i].id]!!,
                    dataMap[indicators[j].id]!!
                )

                val corr = if (alignedA.size >= 2) {
                    pearsonCorrelation(alignedA, alignedB).toBigDecimal().setScale(4, RoundingMode.HALF_UP)
                } else {
                    BigDecimal.ZERO
                }

                matrix[i][j] = corr
                matrix[j][i] = corr
                sampleCounts[i][j] = alignedA.size
                sampleCounts[j][i] = alignedA.size
            }
        }

        return CorrelationMatrixResponse(
            indicatorIds = indicators.map { it.id },
            indicatorNames = indicators.map { it.name },
            matrix = matrix.map { row -> row.toList() },
            sampleCounts = sampleCounts.map { row -> row.toList() },
            startDate = startDate,
            endDate = endDate
        )
    }

    private fun getReturnsByDate(indicatorId: Long, startDate: LocalDate, endDate: LocalDate): Map<LocalDate, Double> {
        val data = indicatorDataRepository.findByIndicatorIdAndDateBetweenOrderByDateAsc(
            indicatorId, startDate, endDate
        )

        if (data.size < 2) return emptyMap()

        val returns = mutableMapOf<LocalDate, Double>()
        for (i in 1 until data.size) {
            val prev = data[i - 1].value.toDouble()
            if (prev != 0.0) {
                val ret = (data[i].value.toDouble() - prev) / prev
                returns[data[i].date] = ret
            }
        }
        return returns
    }

    private fun alignByDate(
        aReturns: Map<LocalDate, Double>,
        bReturns: Map<LocalDate, Double>
    ): Pair<List<Double>, List<Double>> {
        val alignedA = mutableListOf<Double>()
        val alignedB = mutableListOf<Double>()

        for ((date, aVal) in aReturns) {
            val bVal = bReturns[date]
            if (bVal != null) {
                alignedA.add(aVal)
                alignedB.add(bVal)
            }
        }

        return Pair(alignedA, alignedB)
    }

    private fun pearsonCorrelation(a: List<Double>, b: List<Double>): Double {
        val n = a.size
        if (n < 2) return 0.0

        val meanA = a.average()
        val meanB = b.average()

        var sumAB = 0.0
        var sumA2 = 0.0
        var sumB2 = 0.0

        for (i in 0 until n) {
            val da = a[i] - meanA
            val db = b[i] - meanB
            sumAB += da * db
            sumA2 += da * da
            sumB2 += db * db
        }

        val denominator = sqrt(sumA2 * sumB2)
        return if (denominator == 0.0) 0.0 else sumAB / denominator
    }
}
