package com.econdashboard.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(
    name = "indicator_data",
    indexes = [Index(name = "idx_indicator_data_date", columnList = "indicator_id, date")]
)
class IndicatorData(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id", nullable = false)
    var indicator: Indicator,

    @Column(name = "\"date\"", nullable = false)
    var date: LocalDate,

    @Column(name = "\"value\"", nullable = false, precision = 20, scale = 6)
    var value: BigDecimal,

    @Column(name = "\"open\"", precision = 20, scale = 6)
    var open: BigDecimal? = null,

    @Column(precision = 20, scale = 6)
    var high: BigDecimal? = null,

    @Column(precision = 20, scale = 6)
    var low: BigDecimal? = null,

    @Column(name = "\"close\"", precision = 20, scale = 6)
    var close: BigDecimal? = null,

    @Column(precision = 20, scale = 2)
    var volume: BigDecimal? = null,

    @Column(name = "\"change\"", precision = 10, scale = 4)
    var change: BigDecimal? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) : BaseEntity()
