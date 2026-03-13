package com.econdashboard.service

import com.econdashboard.domain.Indicator
import com.econdashboard.dto.IndicatorDataResponse
import com.econdashboard.dto.IndicatorResponse
import com.econdashboard.dto.IndicatorSeriesRequest
import com.econdashboard.dto.IndicatorSeriesResponse
import com.econdashboard.enums.IndicatorCategory
import com.econdashboard.exception.NotFoundException
import com.econdashboard.repository.IndicatorDataRepository
import com.econdashboard.repository.IndicatorRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class IndicatorService(
    private val indicatorRepository: IndicatorRepository,
    private val indicatorDataRepository: IndicatorDataRepository,
    private val indicatorCacheService: IndicatorCacheService
) {

    fun getAllIndicators(category: IndicatorCategory?): List<IndicatorResponse> {
        val indicators = if (category != null) {
            indicatorCacheService.findByCategory(category)
        } else {
            indicatorCacheService.findAllIndicators()
        }
        return indicators.map { IndicatorResponse.from(it) }
    }

    fun getIndicatorById(id: Long): IndicatorResponse {
        val indicator = findIndicatorOrThrow(id)
        return IndicatorResponse.from(indicator)
    }

    fun getCategories(): List<IndicatorCategory> {
        return IndicatorCategory.entries.toList()
    }

    fun getIndicatorData(
        id: Long,
        from: LocalDate?,
        to: LocalDate?,
        pageable: Pageable
    ): Page<IndicatorDataResponse> {
        findIndicatorOrThrow(id)
        val startDate = from ?: LocalDate.now().minusYears(1)
        val endDate = to ?: LocalDate.now()

        return indicatorDataRepository.findByIndicatorIdAndDateBetweenOrderByDateAsc(
            id, startDate, endDate, pageable
        ).map { IndicatorDataResponse.from(it) }
    }

    fun getMultipleIndicatorSeries(request: IndicatorSeriesRequest): List<IndicatorSeriesResponse> {
        return request.indicatorIds.map { indicatorId ->
            findIndicatorOrThrow(indicatorId)
            val data = indicatorCacheService.findSeriesData(
                indicatorId, request.startDate, request.endDate
            )
            IndicatorSeriesResponse(
                indicatorId = indicatorId,
                data = data.map { IndicatorDataResponse.from(it) }
            )
        }
    }

    private fun findIndicatorOrThrow(id: Long): Indicator {
        return indicatorRepository.findById(id)
            .orElseThrow { NotFoundException("Indicator", id) }
    }
}
