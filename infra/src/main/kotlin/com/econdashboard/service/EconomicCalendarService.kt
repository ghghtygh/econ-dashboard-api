package com.econdashboard.service

import com.econdashboard.dto.EconomicEventResponse
import com.econdashboard.enums.EventImportance
import com.econdashboard.exception.NotFoundException
import com.econdashboard.repository.EconomicEventRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class EconomicCalendarService(
    private val economicEventRepository: EconomicEventRepository
) {

    fun getEvents(
        from: LocalDate,
        to: LocalDate,
        country: String?,
        importance: EventImportance?
    ): List<EconomicEventResponse> {
        val fromDateTime = from.atStartOfDay()
        val toDateTime = to.plusDays(1).atStartOfDay()

        val events = when {
            country != null -> economicEventRepository.findByCountryAndEventDateBetweenOrderByEventDateAsc(
                country, fromDateTime, toDateTime
            )
            importance != null -> economicEventRepository.findByEventDateBetweenAndImportanceOrderByEventDateAsc(
                fromDateTime, toDateTime, importance
            )
            else -> economicEventRepository.findByEventDateBetweenOrderByEventDateAsc(
                fromDateTime, toDateTime
            )
        }

        return events.map { EconomicEventResponse.from(it) }
    }

    fun getUpcomingEvents(pageable: Pageable): Page<EconomicEventResponse> {
        return economicEventRepository.findByEventDateAfterOrderByEventDateAsc(
            LocalDateTime.now(), pageable
        ).map { EconomicEventResponse.from(it) }
    }

    fun getEventById(id: Long): EconomicEventResponse {
        val event = economicEventRepository.findById(id)
            .orElseThrow { NotFoundException("EconomicEvent", id) }
        return EconomicEventResponse.from(event)
    }
}
