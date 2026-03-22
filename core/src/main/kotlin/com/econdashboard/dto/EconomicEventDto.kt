package com.econdashboard.dto

import com.econdashboard.domain.EconomicEvent
import com.econdashboard.enums.EventImportance
import java.time.LocalDateTime

data class EconomicEventResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val eventDate: LocalDateTime,
    val country: String,
    val importance: EventImportance,
    val actual: String?,
    val forecast: String?,
    val previous: String?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(event: EconomicEvent) = EconomicEventResponse(
            id = event.id,
            title = event.title,
            description = event.description,
            eventDate = event.eventDate,
            country = event.country,
            importance = event.importance,
            actual = event.actual,
            forecast = event.forecast,
            previous = event.previous,
            createdAt = event.createdAt
        )
    }
}
