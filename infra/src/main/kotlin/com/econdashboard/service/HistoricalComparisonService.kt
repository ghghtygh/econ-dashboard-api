package com.econdashboard.service

import com.econdashboard.dto.*
import com.econdashboard.exception.NotFoundException
import com.econdashboard.repository.IndicatorDataRepository
import com.econdashboard.repository.IndicatorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
@Transactional(readOnly = true)
class HistoricalComparisonService(
    private val indicatorRepository: IndicatorRepository,
    private val indicatorDataRepository: IndicatorDataRepository
) {

    companion object {
        val CRISIS_PRESETS = listOf(
            CrisisPreset(
                id = "gfc-2008",
                name = "2008 글로벌 금융위기",
                description = "리먼 브라더스 파산과 서브프라임 모기지 사태",
                startDate = LocalDate.of(2008, 1, 1),
                endDate = LocalDate.of(2009, 6, 30)
            ),
            CrisisPreset(
                id = "covid-2020",
                name = "2020 코로나 팬데믹",
                description = "COVID-19 글로벌 팬데믹에 따른 경제 충격",
                startDate = LocalDate.of(2020, 1, 1),
                endDate = LocalDate.of(2020, 12, 31)
            ),
            CrisisPreset(
                id = "tightening-2022",
                name = "2022 긴축 사이클",
                description = "인플레이션 대응을 위한 연준의 공격적 금리 인상",
                startDate = LocalDate.of(2022, 1, 1),
                endDate = LocalDate.of(2023, 6, 30)
            )
        )
    }

    fun getPresets(): List<CrisisPreset> = CRISIS_PRESETS

    fun compare(
        indicatorId: Long,
        currentStart: LocalDate,
        currentEnd: LocalDate,
        historicalStart: LocalDate,
        historicalEnd: LocalDate
    ): ComparisonSeriesResponse {
        val indicator = indicatorRepository.findById(indicatorId)
            .orElseThrow { NotFoundException("Indicator", indicatorId) }

        val currentData = indicatorDataRepository.findByIndicatorIdAndDateBetweenOrderByDateAsc(
            indicatorId, currentStart, currentEnd
        )
        val historicalData = indicatorDataRepository.findByIndicatorIdAndDateBetweenOrderByDateAsc(
            indicatorId, historicalStart, historicalEnd
        )

        val currentNormalized = normalizeToIndex(currentData.map { it.date to it.value }, currentStart)
        val historicalNormalized = normalizeToIndex(historicalData.map { it.date to it.value }, historicalStart)

        return ComparisonSeriesResponse(
            indicatorId = indicator.id,
            indicatorName = indicator.name,
            currentPeriod = currentNormalized,
            historicalPeriod = historicalNormalized,
            currentStartDate = currentStart,
            historicalStartDate = historicalStart
        )
    }

    fun compareWithPreset(
        indicatorId: Long,
        presetId: String,
        currentStart: LocalDate,
        currentEnd: LocalDate
    ): ComparisonSeriesResponse {
        val preset = CRISIS_PRESETS.find { it.id == presetId }
            ?: throw IllegalArgumentException("Unknown preset: $presetId")

        return compare(indicatorId, currentStart, currentEnd, preset.startDate, preset.endDate)
    }

    private fun normalizeToIndex(
        data: List<Pair<LocalDate, BigDecimal>>,
        baseDate: LocalDate
    ): List<NormalizedDataPoint> {
        if (data.isEmpty()) return emptyList()

        val baseValue = data.first().second
        if (baseValue.compareTo(BigDecimal.ZERO) == 0) return emptyList()

        return data.map { (date, value) ->
            val dayOffset = ChronoUnit.DAYS.between(baseDate, date).toInt()
            val normalized = value.multiply(BigDecimal(100)).divide(baseValue, 4, RoundingMode.HALF_UP)
            NormalizedDataPoint(
                dayOffset = dayOffset,
                date = date,
                value = value,
                normalizedValue = normalized
            )
        }
    }
}
