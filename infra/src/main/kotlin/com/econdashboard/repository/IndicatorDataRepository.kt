package com.econdashboard.repository

import com.econdashboard.domain.IndicatorData
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface IndicatorDataRepository : JpaRepository<IndicatorData, Long> {

    fun findByIndicatorIdAndDateBetweenOrderByDateAsc(
        indicatorId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<IndicatorData>

    fun findByIndicatorIdAndDateBetweenOrderByDateAsc(
        indicatorId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        pageable: Pageable
    ): Page<IndicatorData>

    fun findTopByIndicatorIdOrderByDateDesc(indicatorId: Long): IndicatorData?

    fun findByIndicatorIdOrderByDateDesc(indicatorId: Long): List<IndicatorData>
}
