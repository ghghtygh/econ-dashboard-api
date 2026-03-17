package com.econdashboard.dto

import com.econdashboard.domain.AlertHistory
import com.econdashboard.domain.AlertRule
import com.econdashboard.enums.AlertConditionType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime

data class AlertRuleRequest(
    @field:NotBlank(message = "사용자 ID는 필수입니다")
    val userId: String,

    @field:NotNull(message = "지표 ID는 필수입니다")
    val indicatorId: Long,

    @field:NotNull(message = "알림 조건 타입은 필수입니다")
    val conditionType: AlertConditionType,

    @field:NotNull(message = "임계값은 필수입니다")
    val threshold: BigDecimal
)

data class AlertRuleResponse(
    val id: Long,
    val userId: String,
    val indicatorId: Long,
    val indicatorName: String,
    val conditionType: AlertConditionType,
    val threshold: BigDecimal,
    val enabled: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(rule: AlertRule) = AlertRuleResponse(
            id = rule.id,
            userId = rule.userId,
            indicatorId = rule.indicator.id,
            indicatorName = rule.indicator.name,
            conditionType = rule.conditionType,
            threshold = rule.threshold,
            enabled = rule.enabled,
            createdAt = rule.createdAt,
            updatedAt = rule.updatedAt
        )
    }
}

data class AlertHistoryResponse(
    val id: Long,
    val userId: String,
    val indicatorId: Long,
    val indicatorName: String,
    val alertRuleId: Long,
    val conditionType: AlertConditionType,
    val threshold: BigDecimal,
    val actualValue: BigDecimal,
    val message: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(history: AlertHistory) = AlertHistoryResponse(
            id = history.id,
            userId = history.userId,
            indicatorId = history.indicator.id,
            indicatorName = history.indicator.name,
            alertRuleId = history.alertRule.id,
            conditionType = history.conditionType,
            threshold = history.threshold,
            actualValue = history.actualValue,
            message = history.message,
            createdAt = history.createdAt
        )
    }
}
