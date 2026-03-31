package com.econdashboard.dto

import com.econdashboard.domain.AlertHistory
import com.econdashboard.domain.AlertRule
import com.econdashboard.enums.AlertConditionType
import com.econdashboard.enums.AlertSeverity
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime

data class AlertRuleRequest(
    @field:NotBlank(message = "사용자 ID는 필수입니다")
    val userId: String,

    @field:NotNull(message = "지표 ID는 필수입니다")
    val indicatorId: Long,

    @field:NotNull(message = "알림 조건은 필수입니다")
    val condition: String,

    @field:NotNull(message = "임계값은 필수입니다")
    val threshold: BigDecimal,

    val severity: String = "warning",

    val message: String = ""
) {
    fun toConditionType(): AlertConditionType = when (condition) {
        "above", "cross_above" -> AlertConditionType.ABOVE
        "below", "cross_below" -> AlertConditionType.BELOW
        "change_pct" -> AlertConditionType.CHANGE_PCT
        else -> AlertConditionType.ABOVE
    }

    fun toSeverity(): AlertSeverity = AlertSeverity.fromValue(severity)
}

data class AlertRuleUpdateRequest(
    val condition: String? = null,
    val threshold: BigDecimal? = null,
    val severity: String? = null,
    val message: String? = null,
    val enabled: Boolean? = null
)

data class AlertRuleResponse(
    val id: String,
    val indicatorId: Long,
    val indicatorName: String,
    val condition: String,
    val threshold: BigDecimal,
    val severity: String,
    val message: String,
    val enabled: Boolean,
    val createdAt: String,
    val updatedAt: String? = null
) {
    companion object {
        private fun conditionTypeToString(type: AlertConditionType): String = when (type) {
            AlertConditionType.ABOVE -> "above"
            AlertConditionType.BELOW -> "below"
            AlertConditionType.CHANGE_PCT -> "change_pct"
        }

        fun from(rule: AlertRule) = AlertRuleResponse(
            id = rule.id.toString(),
            indicatorId = rule.indicator.id,
            indicatorName = rule.indicator.name,
            condition = conditionTypeToString(rule.conditionType),
            threshold = rule.threshold,
            severity = rule.severity.value,
            message = rule.message,
            enabled = rule.enabled,
            createdAt = rule.createdAt.toString(),
            updatedAt = rule.updatedAt.toString()
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
