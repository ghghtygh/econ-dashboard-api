package com.econdashboard.repository

import com.econdashboard.domain.EconomicEvent
import com.econdashboard.enums.EventImportance
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface EconomicEventRepository : JpaRepository<EconomicEvent, Long> {

    fun findByEventDateBetweenOrderByEventDateAsc(
        from: LocalDateTime,
        to: LocalDateTime
    ): List<EconomicEvent>

    fun findByEventDateBetweenAndImportanceOrderByEventDateAsc(
        from: LocalDateTime,
        to: LocalDateTime,
        importance: EventImportance
    ): List<EconomicEvent>

    fun findByCountryAndEventDateBetweenOrderByEventDateAsc(
        country: String,
        from: LocalDateTime,
        to: LocalDateTime
    ): List<EconomicEvent>

    fun findByEventDateAfterOrderByEventDateAsc(
        after: LocalDateTime,
        pageable: Pageable
    ): Page<EconomicEvent>
}
