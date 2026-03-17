package com.econdashboard.domain

import com.econdashboard.enums.AlertConditionType
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "alert_history")
class AlertHistory(
    @Column(name = "user_id", nullable = false)
    var userId: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id", nullable = false)
    var indicator: Indicator,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_rule_id", nullable = false)
    var alertRule: AlertRule,

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false)
    var conditionType: AlertConditionType,

    @Column(nullable = false, precision = 20, scale = 6)
    var threshold: BigDecimal,

    @Column(name = "actual_value", nullable = false, precision = 20, scale = 6)
    var actualValue: BigDecimal,

    @Column(nullable = false, length = 500)
    var message: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) : BaseEntity()
