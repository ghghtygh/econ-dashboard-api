package com.econdashboard.domain

import com.econdashboard.enums.ChartType
import jakarta.persistence.*

@Entity
@Table(name = "dashboard_widgets")
class DashboardWidget(

    @Column(nullable = false)
    var title: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var chartType: ChartType,

    @Column(nullable = false)
    var positionX: Int,

    @Column(nullable = false)
    var positionY: Int,

    @Column(nullable = false)
    var width: Int = 1,

    @Column(nullable = false)
    var height: Int = 1,

    @Column(columnDefinition = "TEXT")
    var config: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id")
    var indicator: Indicator? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) : BaseEntity()
