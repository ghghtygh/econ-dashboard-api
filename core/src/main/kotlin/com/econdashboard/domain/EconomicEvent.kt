package com.econdashboard.domain

import com.econdashboard.enums.EventImportance
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "economic_events",
    indexes = [
        Index(name = "idx_economic_events_event_date", columnList = "event_date"),
        Index(name = "idx_economic_events_country", columnList = "country")
    ]
)
class EconomicEvent(

    @Column(nullable = false, length = 500)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "event_date", nullable = false)
    var eventDate: LocalDateTime,

    @Column(nullable = false, length = 10)
    var country: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var importance: EventImportance,

    @Column(length = 100)
    var actual: String? = null,

    @Column(length = 100)
    var forecast: String? = null,

    @Column(length = 100)
    var previous: String? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) : BaseEntity()
