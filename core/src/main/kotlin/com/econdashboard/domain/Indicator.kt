package com.econdashboard.domain

import com.econdashboard.enums.DataSource
import com.econdashboard.enums.IndicatorCategory
import jakarta.persistence.*

@Entity
@Table(name = "indicators")
class Indicator(

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var symbol: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: IndicatorCategory,

    @Column(nullable = false)
    var unit: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var source: DataSource,

    var description: String? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) : BaseEntity()
