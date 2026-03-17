package com.econdashboard.domain

import com.econdashboard.enums.AlertConditionType
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "alert_rules")
class AlertRule(
    @Column(name = "user_id", nullable = false)
    var userId: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id", nullable = false)
    var indicator: Indicator,

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false)
    var conditionType: AlertConditionType,

    @Column(nullable = false, precision = 20, scale = 6)
    var threshold: BigDecimal,

    @Column(nullable = false)
    var enabled: Boolean = true,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) : BaseEntity()
