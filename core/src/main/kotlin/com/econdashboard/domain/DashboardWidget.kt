package com.econdashboard.domain

import com.econdashboard.enums.ChartType
import jakarta.persistence.*

@Entity
@Table(name = "dashboard_widgets")
class DashboardWidget(

    @Column(nullable = false)
    var title: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "chart_type", nullable = false)
    var chartType: ChartType,

    @Column(name = "position_x", nullable = false)
    var positionX: Int,

    @Column(name = "position_y", nullable = false)
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
